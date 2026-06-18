// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package com.YD0304.sushi_shop.service;

import com.YD0304.sushi_shop.entity.Status;
import com.YD0304.sushi_shop.entity.Sushi;
import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.dto.OrderStatusDto;
import com.YD0304.sushi_shop.repository.StatusRepository;
import com.YD0304.sushi_shop.repository.SushiOrderRepository;
import com.YD0304.sushi_shop.repository.SushiRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SushiService {
    private final SushiRepository sushiRepository;
    private final StatusRepository statusRepository;
    private final SushiOrderRepository sushiOrderRepository;
    private final ExecutorService chefExecutor = Executors.newFixedThreadPool(3);

    // Track worker threads per order
    private final ConcurrentHashMap<Integer, Thread> workerThreads = new ConcurrentHashMap<>();

    public SushiService(SushiRepository sushiRepository, StatusRepository statusRepository, SushiOrderRepository sushiOrderRepository) {
        this.sushiRepository = sushiRepository;
        this.statusRepository = statusRepository;
        this.sushiOrderRepository = sushiOrderRepository;
    }

    @Transactional
    public SushiOrder createSushiOrder(String sushiName) {
        Sushi sushi = this.sushiRepository.findByName(sushiName).orElseThrow();
        Status createdStatus = this.statusRepository.findByName("created").orElseThrow();

        SushiOrder sushiOrder = new SushiOrder();
        sushiOrder.setSushi(sushi);
        sushiOrder.setStatus(createdStatus);
        sushiOrder = this.sushiOrderRepository.save(sushiOrder);

        submitOrderProcessingTask(sushiOrder.getId());
        return sushiOrder;
    }

    private void submitOrderProcessingTask(Integer sushiOrderId) {
        chefExecutor.execute(() -> processSushiOrder(sushiOrderId));
    }

    private void processSushiOrder(Integer sushiOrderId) {
        try {
            var initialOrder = this.sushiOrderRepository.findById(sushiOrderId).orElseThrow();
            String currentStatus = initialOrder.getStatus().getName();
            if ("cancelled".equals(currentStatus) || "finished".equals(currentStatus)) {
                return;
            }

            SushiOrder order = changeSushiOrderStatus(sushiOrderId, "in-progress");
            int totalSeconds = Optional.ofNullable(order.getSushi())
                    .map(Sushi::getTimeToMake)
                    .orElse(0);
            long totalMillis = totalSeconds * 1000L;

            long createdTime = order.getCreatedAt().getTime();
            Thread current = Thread.currentThread();
            workerThreads.put(sushiOrderId, current);

            final long chunk = 200L;
            while (true) {
                long elapsed = System.currentTimeMillis() - createdTime;
                long remaining = Math.max(0, totalMillis - elapsed);
                
                if (remaining <= 0) {
                    break;
                }

                long sleep = Math.min(chunk, remaining);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ie) {
                    var fresh = this.sushiOrderRepository.findById(sushiOrderId).orElseThrow();
                    String s = fresh.getStatus().getName();
                    if ("cancelled".equals(s)) {
                        workerThreads.remove(sushiOrderId);
                        return;
                    } else if ("paused".equals(s)) {
                        workerThreads.remove(sushiOrderId);
                        return;
                    } else {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            workerThreads.remove(sushiOrderId);
            changeSushiOrderStatus(sushiOrderId, "finished");

        } catch (RuntimeException e) {
            throw e;
        }
    }


    @Transactional
    public SushiOrder changeSushiOrderStatus(Integer orderId, String statusName) {
        SushiOrder sushiOrder = this.sushiOrderRepository.findById(orderId).orElseThrow();
        Status newStatus = this.statusRepository.findByName(statusName).orElseThrow();
        sushiOrder.setStatus(newStatus);
        return this.sushiOrderRepository.save(sushiOrder);
    }

    @Transactional
    public void cancelSushiOrder(Integer orderId) {
        SushiOrder sushiOrder = this.sushiOrderRepository.findById(orderId).orElseThrow();
        String currentStatus = sushiOrder.getStatus().getName();
        if ("created".equals(currentStatus)) {
            changeSushiOrderStatus(orderId, "cancelled");
            return;
        }

        if ("finished".equals(currentStatus)) {
            throw new IllegalStateException("Cannot cancel an order that is already finished");
        }

        if ("cancelled".equals(currentStatus)) {
            throw new IllegalStateException("Order is already cancelled");
        }

        // For in-progress or paused: set cancelled and interrupt worker if running
        changeSushiOrderStatus(orderId, "cancelled");
        // interrupt worker thread if present
        Thread t = workerThreads.get(orderId);
        if (t != null) t.interrupt();
    }

    @Transactional
    public SushiOrder pauseSushiOrder(int orderId) {
        SushiOrder sushiOrder = this.sushiOrderRepository.findById(orderId).orElseThrow();
        String currentStatus = sushiOrder.getStatus().getName();
        if ("in-progress".equals(currentStatus)) {
            changeSushiOrderStatus(orderId, "paused");
            Thread t = workerThreads.get(orderId);
            if (t != null) t.interrupt();
            return this.sushiOrderRepository.findById(orderId).orElseThrow();
        } else {
            throw new IllegalStateException("Only orders in progress can be paused");
        }
    }

    @Transactional
    public SushiOrder resumeSushiOrder(int orderId) {
        SushiOrder sushiOrder = this.sushiOrderRepository.findById(orderId).orElseThrow();
        String currentStatus = sushiOrder.getStatus().getName();
        if ("paused".equals(currentStatus)) {
            processSushiOrder(orderId);
            return changeSushiOrderStatus(orderId, "in-progress");
        } else {
            throw new IllegalStateException("Only paused orders can be resumed");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, List<OrderStatusDto>> getOrdersByStatus() {
        return this.sushiOrderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        order -> normalizeStatusName(order.getStatus().getName()),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                order -> new OrderStatusDto(order.getId(), calculateTimeSpent(order)),
                                Collectors.toList()
                        )
                ));
    }

    private int calculateTimeSpent(SushiOrder order) {
        int totalSeconds = Optional.ofNullable(order.getSushi())
                .map(Sushi::getTimeToMake)
                .orElse(0);

        String status = order.getStatus().getName();
        if ("created".equals(status)) {
            return 0;
        }
        if ("finished".equals(status)) {
            return totalSeconds;
        }

        long elapsedMillis = System.currentTimeMillis() - order.getCreatedAt().getTime();
        long elapsedSeconds = elapsedMillis / 1000;
        return (int) Math.min(totalSeconds, elapsedSeconds);
    }

    private String normalizeStatusName(String statusName) {
        return "finished".equals(statusName) ? "completed" : statusName;
    }
}
