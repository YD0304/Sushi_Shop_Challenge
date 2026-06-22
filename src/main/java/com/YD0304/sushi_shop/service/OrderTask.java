package com.YD0304.sushi_shop.service;

public class OrderTask implements Runnable, Comparable<OrderTask> {

    private final Integer orderId;
    private final int priority;
    private final int sequence;
    private final SushiOrderService sushiOrderService;
    private final OrderScheduler orderScheduler;
    private volatile Thread workerThread;

    public OrderTask(int orderId, int priority, int sequence, SushiOrderService sushiOrderService,
            OrderScheduler orderScheduler) {
        this.orderId = orderId;
        this.priority = priority;
        this.sequence = sequence;
        this.sushiOrderService = sushiOrderService;
        this.orderScheduler = orderScheduler;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void interrupt() {
        Thread thread = workerThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public int compareTo(OrderTask other) {
        int cmp = Integer.compare(this.priority, other.priority);
        if (cmp != 0) return cmp;

        return Integer.compare(this.sequence, other.sequence);
    }


    @Override
    public void run() {
        workerThread = Thread.currentThread();
        try {
            sushiOrderService.processSushiOrder(orderId);
        } finally {
            workerThread = null;
            orderScheduler.taskFinished(orderId, this);
        }
    }
}
