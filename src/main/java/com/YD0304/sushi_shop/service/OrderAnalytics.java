package com.YD0304.sushi_shop.service;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OrderAnalytics {


    private final int orderId;
    private Instant inProgressStart;
    private long totalMakeTimeMillis;
    private boolean isPause = false;

    OrderAnalytics(int orderId) {
        this.orderId = orderId;
    }

    synchronized void start() {
        if (inProgressStart != null) {
            // This shouldn't happen if paused correctly, but we'll accumulate just in case.
            long elapsed = Duration.between(inProgressStart, Instant.now()).toMillis();
            totalMakeTimeMillis += elapsed;
        }
        inProgressStart = Instant.now();
        isPause = false;
    }

    synchronized void pause() {
        if (inProgressStart != null && !isPause) {
            long elapsed = Duration.between(inProgressStart, Instant.now()).toMillis();
            totalMakeTimeMillis += elapsed;
            inProgressStart = null;
            isPause = true;
        } else {
        }
    }

    synchronized void finish() {
        if (inProgressStart != null) {
            long elapsed = Duration.between(inProgressStart, Instant.now()).toMillis();
            totalMakeTimeMillis += elapsed;
            inProgressStart = null;}
        isPause = false;
    }

    synchronized long getTotalMakeTimeMillis() {
        if (inProgressStart == null) {
            return totalMakeTimeMillis;
        }
        // Add the currently running interval
        long currentInterval = Duration.between(inProgressStart, Instant.now()).toMillis();
        long total = totalMakeTimeMillis + currentInterval;
        return total;
    }

    int getOrderId() {
        return orderId;
    }
}