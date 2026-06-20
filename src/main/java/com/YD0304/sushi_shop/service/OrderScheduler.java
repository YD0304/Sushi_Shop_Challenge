package com.YD0304.sushi_shop.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Lazy;

@Component
public class OrderScheduler {

    private static final int CHEF_COUNT = 3;

    private final BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
    private final ThreadPoolExecutor executor;
    private final Map<Integer, Future<?>> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger orderSequence = new AtomicInteger();

    public OrderScheduler() {
        // Pass the queue explicitly so the executor actually uses priority ordering.
        // With core==max, tasks that arrive while all threads are busy queue here.
        this.executor = new ThreadPoolExecutor(
            CHEF_COUNT, CHEF_COUNT, 0L, TimeUnit.MILLISECONDS, queue
        );
    }

    public void enqueue(int orderId, int priority, SushiOrderService service) {
        int seq = orderSequence.getAndIncrement();
        OrderTask task = new OrderTask(orderId, priority, seq);
        Future<?> future = executor.submit(task);
        tasks.put(orderId, future);
    }

    public void cancel(int orderId) {
        Future<?> future = tasks.remove(orderId);
        if (future != null) future.cancel(true);
        // Status change is handled by the caller (SushiOrderService)
    }

    public void pause(int orderId) {
        Future<?> future = tasks.remove(orderId);
        if (future != null) future.cancel(true);
        // Status change is handled by the caller (SushiOrderService)
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}