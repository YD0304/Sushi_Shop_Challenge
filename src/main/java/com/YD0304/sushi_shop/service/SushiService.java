// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package com.YD0304.sushi_shop.service;

import com.YD0304.sushi_shop.entity.Status;
import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.repository.StatusRepository;
import com.YD0304.sushi_shop.repository.SushiOrderRepository;
import com.YD0304.sushi_shop.repository.SushiRepository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SushiService {

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

    @Transactional
    public SushiOrder createSushiOrder(String sushiName) {
        var sushi = this.sushiRepository.findByName(sushiName).orElseThrow();
        var createdStatus = this.statusRepository.findByName("created").orElseThrow();
        SushiOrder sushiOrder = new SushiOrder(createdStatus, sushi);
        sushiOrder = this.sushiOrderRepository.save(sushiOrder);
        // start processing asynchronously
        processSushiOrder(sushiOrder.getId());
        return sushiOrder;
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
        remainingMillis.remove(orderId);
    }

    @Transactional
    public SushiOrder pauseSushiOrder(int orderId) {
        SushiOrder sushiOrder = this.sushiOrderRepository.findById(orderId).orElseThrow();
        String currentStatus = sushiOrder.getStatus().getName();
        if ("in-progress".equals(currentStatus)) {
            // request pause: set DB status to paused and interrupt worker to persist remaining time
            changeSushiOrderStatus(orderId, "paused");
            Thread t = workerThreads.get(orderId);
            if (t != null) t.interrupt();
            // remaining time will be updated by worker when it handles interrupt
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
            // processing will pick up remainingTime from DB
            processSushiOrder(orderId);
            return changeSushiOrderStatus(orderId, "in-progress");
        } else {
            throw new IllegalStateException("Only paused orders can be resumed");
        }
    }

}