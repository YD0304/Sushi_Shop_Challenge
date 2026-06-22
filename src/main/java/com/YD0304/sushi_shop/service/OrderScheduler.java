package com.YD0304.sushi_shop.service;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class OrderScheduler {

    private static final int CHEF_COUNT = 3;

    private final BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
    private final ThreadPoolExecutor executor;
    private final Map<Integer, OrderTask> tasks = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicInteger> timeSpentByOrder = new ConcurrentHashMap<>();
    private final AtomicInteger orderSequence = new AtomicInteger();

    public OrderScheduler() {

        this.executor = new ThreadPoolExecutor(
            CHEF_COUNT, CHEF_COUNT, 0L, TimeUnit.MILLISECONDS, queue
        );
    }

    public void enqueue(int orderId, int priority, SushiOrderService service) {
        int seq = orderSequence.getAndIncrement();
        OrderTask task = new OrderTask(orderId, priority, seq, service, this);
        OrderTask existingTask = tasks.put(orderId, task);
        if (existingTask != null) {
            executor.remove(existingTask);
            existingTask.interrupt();
        }
        executor.execute(task);
    }

    public void cancel(int orderId) {
        stop(orderId);
    }

    public void pause(int orderId) {
        stop(orderId);
    }

    public int getTimeSpent(int orderId) {
        return timeSpentByOrder.computeIfAbsent(orderId, ignored -> new AtomicInteger()).get();
    }

    public int incrementTimeSpent(int orderId) {
        return timeSpentByOrder.computeIfAbsent(orderId, ignored -> new AtomicInteger()).incrementAndGet();
    }

    void taskFinished(int orderId, OrderTask task) {
        tasks.remove(orderId, task);
    }

    private void stop(int orderId) {
        OrderTask task = tasks.remove(orderId);
        if (task == null) {
            return;
        }
        executor.remove(task);
        task.interrupt();
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
