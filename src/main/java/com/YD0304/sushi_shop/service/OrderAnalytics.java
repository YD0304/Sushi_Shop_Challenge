package com.YD0304.sushi_shop.service;

import java.time.Duration;
import java.time.Instant;

class OrderAnalytics {

    private final int orderId;
    private Instant inProgressStart;
    private long totalMakeTimeMillis;
    private boolean isPause = false;

    OrderAnalytics(int orderId) {
        this.orderId = orderId;
    }

    synchronized void start() {
        inProgressStart = Instant.now();
        isPause = false;
    }

    synchronized void pause() {
        if (inProgressStart != null && !isPause) {
            totalMakeTimeMillis += Duration.between(inProgressStart, Instant.now()).toMillis();
            inProgressStart = null;
            isPause = true;
        }
    }

    synchronized void finish() {
        if (inProgressStart != null) {
            totalMakeTimeMillis += Duration.between(inProgressStart, Instant.now()).toMillis();
            inProgressStart = null;
        }
        isPause = false;
    }

    synchronized long getTotalMakeTimeMillis() {
        if (inProgressStart == null) {
            return totalMakeTimeMillis;
        }
        return totalMakeTimeMillis + Duration.between(inProgressStart, Instant.now()).toMillis();
    }

    int getOrderId() {
        return orderId;
    }
}