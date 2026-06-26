package com.YD0304.sushi_shop.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.YD0304.sushi_shop.dto.AnalyticsResponse;
import com.YD0304.sushi_shop.dto.OrderStatusResponse;
import com.YD0304.sushi_shop.entity.Status;
import com.YD0304.sushi_shop.entity.Sushi;
import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.repository.StatusRepository;
import com.YD0304.sushi_shop.repository.SushiOrderRepository;
import com.YD0304.sushi_shop.repository.SushiRepository;


@Service
public class SushiOrderService {
    private final SushiRepository sushiRepository;
    private final StatusRepository statusRepository;
    private final SushiOrderRepository sushiOrderRepository;
    private final OrderScheduler orderScheduler;
    private final AnalyticsService analyticsService;

    public SushiOrderService(SushiRepository sushiRepository, StatusRepository statusRepository,
            SushiOrderRepository sushiOrderRepository, OrderScheduler orderScheduler,
            AnalyticsService analyticsService) {
        this.sushiRepository = sushiRepository;
        this.statusRepository = statusRepository;
        this.sushiOrderRepository = sushiOrderRepository;
        this.orderScheduler = orderScheduler;
        this.analyticsService = analyticsService;
    }

    @Transactional
    public SushiOrder createSushiOrder(String sushiName) {
        Sushi sushi = this.sushiRepository.findByName(sushiName)
                .orElseThrow(() -> new IllegalArgumentException("Sushi not found"));
        Status createdStatus = this.statusRepository.findByName("created")
                .orElseThrow(() -> new IllegalArgumentException("Status not found"));

        SushiOrder sushiOrder = new SushiOrder();
        sushiOrder.setSushi(sushi);
        sushiOrder.setStatus(createdStatus);
        SushiOrder savedSushiOrder = this.sushiOrderRepository.save(sushiOrder);
        analyticsService.created(savedSushiOrder.getId(), sushi.getName(), savedSushiOrder.getCreatedAt());

        int FIFO_PRIORITY = 1;
        orderScheduler.enqueue(savedSushiOrder.getId(), FIFO_PRIORITY, this);
        return savedSushiOrder;
    }

    public void processSushiOrder(Integer sushiOrderId) {
        SushiOrder order = sushiOrderRepository.findById(sushiOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!canProcess(order)) {
            return;
        }

        changeSushiOrderStatus(sushiOrderId, "in-progress");
        analyticsService.started(sushiOrderId);

        try {
            int cookingTimeSeconds = order.getSushi().getTimeToMake();

            while (orderScheduler.getTimeSpent(sushiOrderId) < cookingTimeSeconds) {
                // Refresh order status from the database
                SushiOrder currentOrder = sushiOrderRepository.findById(sushiOrderId)
                        .orElseThrow(() -> new IllegalArgumentException("Order not found"));
                String currentStatus = currentOrder.getStatus().getName();

                if ("cancelled".equals(currentStatus) || "finished".equals(currentStatus)) {
                    return; // order was cancelled or already finished – stop processing
                }
                if ("paused".equals(currentStatus)) {
                    Thread.sleep(100L);
                    continue; // do not increment time
                }

                // Not paused or cancelled
                Thread.sleep(1000L);
                orderScheduler.incrementTimeSpent(sushiOrderId);
            }

            // Only mark finished if still in-progress (not cancelled/paused mid-flight)
            SushiOrder finalOrder = sushiOrderRepository.findById(sushiOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            if ("in-progress".equals(finalOrder.getStatus().getName())) {
                changeSushiOrderStatus(sushiOrderId, "finished");
                analyticsService.finished(sushiOrderId);}
     
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

        

    @Transactional
    public SushiOrder changeSushiOrderStatus(Integer orderId, String statusName) {
        SushiOrder sushiOrder = this.sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Sushi order not found"));
        Status newStatus = this.statusRepository.findByName(statusName)
                .orElseThrow(() -> new IllegalArgumentException("Status not found"));
        sushiOrder.setStatus(newStatus);
        return this.sushiOrderRepository.save(sushiOrder);
    }

 
    @Transactional
    public void cancelSushiOrder(Integer orderId) {
        SushiOrder order = sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        String currentStatus = order.getStatus().getName();
        if ("finished".equals(currentStatus) || "cancelled".equals(currentStatus)) {
            throw new IllegalStateException("Order cannot be cancelled from status: " + currentStatus);
        }
        orderScheduler.cancel(orderId);
        changeSushiOrderStatus(orderId, "cancelled");
        analyticsService.cancelled(orderId);
    }

  
    @Transactional
    public void pauseSushiOrder(Integer orderId) {
        SushiOrder order = sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!"in-progress".equals(order.getStatus().getName())) {
            throw new IllegalStateException("Only in-progress orders can be paused");
        }
        orderScheduler.pause(orderId);
        changeSushiOrderStatus(orderId, "paused");
        analyticsService.paused(orderId);
    }

    @Transactional
    public void resumeSushiOrder(int orderId) {
        SushiOrder order = sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!"paused".equals(order.getStatus().getName())) {
            throw new IllegalStateException("Only paused orders can be resumed");
        }
      
        changeSushiOrderStatus(orderId, "created");
        int HIGH_PRIORITY = 0;
        orderScheduler.enqueue(orderId, HIGH_PRIORITY, this);
    }

    public Map<String, List<OrderStatusResponse>> getOrdersGroupedByStatus() {
        List<SushiOrder> allOrders = sushiOrderRepository.findAll();
        return allOrders.stream()
            .collect(Collectors.groupingBy(
                order -> updateStatusName(order.getStatus().getName()),
                Collectors.mapping(order -> {
                    OrderStatusResponse dto = new OrderStatusResponse();
                    dto.setOrderId(order.getId());
                    dto.setTimeSpent(orderScheduler.getTimeSpent(order.getId()));
                    return dto;
                }, Collectors.toList())
            ));
    }

    public int getTimeSpent(Integer orderId) {
        return orderScheduler.getTimeSpent(orderId);
    }

    public AnalyticsResponse getAnalytics() {
        return analyticsService.getAnalytics();
    }

    private String updateStatusName(String statusName) {
        return "finished".equals(statusName) ? "completed" : statusName;
    }

    private boolean canProcess(SushiOrder order) {
        return "created".equals(order.getStatus().getName());
    }
    
}