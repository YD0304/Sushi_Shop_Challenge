package com.YD0304.sushi_shop.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public SushiOrderService(SushiRepository sushiRepository, StatusRepository statusRepository, SushiOrderRepository sushiOrderRepository, OrderScheduler orderScheduler) {
        this.sushiRepository = sushiRepository;
        this.statusRepository = statusRepository;
        this.sushiOrderRepository = sushiOrderRepository;
        this.orderScheduler = orderScheduler;
    }

    @Transactional
    public SushiOrder createSushiOrder(String sushiName) {
        Sushi sushi = this.sushiRepository.findByName(sushiName)
                .orElseThrow(() -> new IllegalArgumentException("Sushi not found"));
        Status createdStatus = this.statusRepository.findByName("created")
                .orElseThrow(() -> new IllegalArgumentException("status not found"));

        SushiOrder sushiOrder = new SushiOrder();
        sushiOrder.setSushi(sushi);
        sushiOrder.setStatus(createdStatus);
        SushiOrder savedSushiOrder = this.sushiOrderRepository.save(sushiOrder);

        int FIFO_Priority = 1;
        orderScheduler.enqueue(savedSushiOrder.getId(), FIFO_Priority, this);
        return savedSushiOrder;
    }

    @Transactional
    public void processSushiOrder(Integer sushiOrderId) {

        SushiOrder order = sushiOrderRepository.findById(sushiOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!canProcess(order)) {
            return;
        }

        changeSushiOrderStatus(sushiOrderId, "in-progress");

        try {
            int cookingTimeSeconds = order.getSushi().getTimeToMake();

            while (orderScheduler.getTimeSpent(sushiOrderId) < cookingTimeSeconds) {
                Thread.sleep(1000L);
                orderScheduler.incrementTimeSpent(sushiOrderId);
            }

            SushiOrder currentOrder = sushiOrderRepository.findById(sushiOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            if ("in-progress".equals(currentOrder.getStatus().getName())) {
                changeSushiOrderStatus(sushiOrderId, "finished");
            }

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
        orderScheduler.cancel(orderId);
        changeSushiOrderStatus(orderId, "cancelled");
    }

    @Transactional
    public void pauseSushiOrder(Integer orderId) {
        orderScheduler.pause(orderId);
        changeSushiOrderStatus(orderId, "paused");
    }

    @Transactional
    public void resumeSushiOrder(int orderId) {
        changeSushiOrderStatus(orderId, "created");
        int highPriority = 0;
        orderScheduler.enqueue(orderId, highPriority, this);

    }

    public Map<String, List<OrderStatusResponse>> getOrdersGroupedByStatus() {
        List<SushiOrder> allOrders = sushiOrderRepository.findAll();
        return allOrders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getStatus().getName(),
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

    private String updateStatusName(String statusName) {
        return "finished".equals(statusName) ? "completed" : statusName;
    }

    private boolean canProcess(SushiOrder order) {
        return "created".equals(order.getStatus().getName());
    }
}
