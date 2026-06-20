package com.YD0304.sushi_shop.service;

import com.YD0304.sushi_shop.entity.Status;
import com.YD0304.sushi_shop.entity.Sushi;
import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.dto.OrderStatusDto;
import com.YD0304.sushi_shop.repository.StatusRepository;
import com.YD0304.sushi_shop.repository.SushiOrderRepository;
import com.YD0304.sushi_shop.repository.SushiRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SushiOrderService {

    private SushiOrderService self;
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

        if (isTerminal(order)) {
            return;
        }

        changeSushiOrderStatus(sushiOrderId, "in-progress");

        try {
            long cookingTime = order.getSushi().getTimeToMake() * 1000L;

            Thread.sleep(cookingTime); // executor thread handles this

            changeSushiOrderStatus(sushiOrderId, "finished");

        } catch (InterruptedException e) {

            changeSushiOrderStatus(sushiOrderId, "paused");

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

    // @Transactional
    // public Map<String, List<OrderStatusDto>> getOrdersByStatus() {
    //     Map<String, List<OrderStatusDto>> result = new LinkedHashMap<>();

    //     for (SushiOrder order : this.sushiOrderRepository.findAll()) {
    //         String displayStatus = updateStatusName(order.getStatus().getName());
    //         OrderStatusDto dto = new OrderStatusDto(order.getId(), calculateTimeSpent(order));
    //         result.computeIfAbsent(displayStatus, key -> new ArrayList<>()).add(dto);
    //     }
    //     return result;
    // }

    private String updateStatusName(String statusName) {
        return "finished".equals(statusName) ? "completed" : statusName;
    }

    private boolean isTerminal(SushiOrder order) {
        String status = order.getStatus().getName();
        return "cancelled".equals(status) || "finished".equals(status);
    }
}