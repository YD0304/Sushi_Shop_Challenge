package com.YD0304.sushi_shop.service;

public class OrderTask implements Runnable, Comparable<OrderTask> {

    private  Integer orderId;
    private  int priority;
    private  int sequence;
    private  SushiOrderService service;

    public OrderTask(int orderId, int priority, int sequence) {
        this.orderId = orderId;
        this.priority = priority;
        this.sequence = sequence;
    }

    public Integer getOrderId() {
        return orderId;
    }

    @Override
    public int compareTo(OrderTask other) {
        int cmp = Integer.compare(this.priority, other.priority);
        if (cmp != 0) return cmp;

        return Integer.compare(this.sequence, other.sequence);
    }


    @Override
    public void run() {
        service.processSushiOrder(orderId);
    }
}