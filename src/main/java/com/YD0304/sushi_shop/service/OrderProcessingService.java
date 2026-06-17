package com.YD0304.sushi_shop.service;

import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.repository.StatusRepository;
import com.YD0304.sushi_shop.repository.SushiOrderRepository;
import com.YD0304.sushi_shop.repository.SushiRepository;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class OrderProcessingService {
    private final SushiRepository sushiRepository;
    private final StatusRepository statusRepository;
    private final SushiOrderRepository sushiOrderRepository;
    private static final Semaphore chefs = new Semaphore(3);

    // Track worker threads and remaining milliseconds per order in-memory
    private final ConcurrentHashMap<Integer, Thread> workerThreads = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Long> remainingMillis = new ConcurrentHashMap<>();

    public SushiService(SushiRepository sushiRepository, StatusRepository statusRepository, SushiOrderRepository sushiOrderRepository) {
        this.sushiRepository = sushiRepository;
        this.statusRepository = statusRepository;
        this.sushiOrderRepository = sushiOrderRepository;
    }

    @Async
    public void processSushiOrder(Integer sushiOrderId) {
        try {
            chefs.acquire();
            // mark in-progress (uses its own transaction)
            SushiOrder order = changeSushiOrderStatus(sushiOrderId, "in-progress");

            long remaining = order.getRemainingTime() != null ? order.getRemainingTime() : ((order.getSushi().getTimeToMake() != null ? order.getSushi().getTimeToMake() : 0) * 1000L);
            remainingMillis.put(sushiOrderId, remaining);

            Thread current = Thread.currentThread();
            workerThreads.put(sushiOrderId, current);

            final long chunk = 200L;
            while (remaining > 0) {
                long sleep = Math.min(chunk, remaining);
                try {
                    Thread.sleep(sleep);
                    remaining -= sleep;
                    remainingMillis.put(sushiOrderId, remaining);
                } catch (InterruptedException ie) {
                    // check status from DB
                    var fresh = this.sushiOrderRepository.findById(sushiOrderId).orElseThrow();
                    String s = fresh.getStatus().getName();
                    if ("cancelled".equals(s)) {
                        remainingMillis.remove(sushiOrderId);
                        workerThreads.remove(sushiOrderId);
                        return;
                    } else if ("paused".equals(s)) {
                        // ensure remaining stored in DB
                        fresh.setRemainingTime(remaining);
                        this.sushiOrderRepository.save(fresh);
                        remainingMillis.remove(sushiOrderId);
                        workerThreads.remove(sushiOrderId);
                        return;
                    } else {
                        // unexpected interrupt: restore interrupt flag and continue
                        Thread.currentThread().interrupt();
                    }
                }
            }

            remainingMillis.remove(sushiOrderId);
            workerThreads.remove(sushiOrderId);
            changeSushiOrderStatus(sushiOrderId, "finished");
        } catch (RuntimeException e) {
            throw e;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sushi preparation interrupted", ie);
        } finally {
            chefs.release();
        }
    }
}
