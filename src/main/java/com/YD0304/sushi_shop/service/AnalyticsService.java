package com.YD0304.sushi_shop.service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.YD0304.sushi_shop.dto.AnalyticsResponse;

@Service
public class AnalyticsService {
    private static final int CHEF_COUNT = 3;

    private final AtomicLong totalWaitMillis = new AtomicLong();
    private final AtomicLong totalMakeMillis = new AtomicLong();
    private final AtomicLong totalOrdersStarted = new AtomicLong();
    private final AtomicLong totalOrdersFinished = new AtomicLong();

    private final ConcurrentHashMap<Integer, OrderAnalytics> orders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Instant> orderCreatedAt = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Boolean> orderStarted = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> ordersBySushi = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, AtomicLong> ordersByHour = new ConcurrentHashMap<>();

    private final Instant serverStartTime = Instant.now();

    public void created(int orderId, String sushiName, Instant createdAt) {
        Instant createdInstant;
        int hour;

        if (createdAt == null) {
            createdInstant = Instant.now();
            hour = createdInstant.atZone(ZoneId.systemDefault()).getHour();
        } else {
            createdInstant = createdAt;
            hour = createdAt.atZone(ZoneId.systemDefault()).getHour();
        }

        orders.put(orderId, new OrderAnalytics(orderId));
        orderCreatedAt.put(orderId, createdInstant);
        addSushiCount(sushiName);
        addHourCount(hour);

    }

    public void started(int orderId) {
        OrderAnalytics order = orders.get(orderId);
        if (order == null) {
            order = new OrderAnalytics(orderId);
            orders.put(orderId, order);
        }

        order.start();

        if (!orderStarted.containsKey(orderId)) {
            Instant createdAt = orderCreatedAt.get(orderId);
            if (createdAt != null) {
                long waitMillis = Duration.between(createdAt, Instant.now()).toMillis();
                totalWaitMillis.addAndGet(waitMillis);
                totalOrdersStarted.incrementAndGet();
                orderStarted.put(orderId, true);}
        }
    }

    public void paused(int orderId) {
        OrderAnalytics order = orders.get(orderId);
        if (order != null) {
            order.pause();
    }}

    public void finished(int orderId) {
        OrderAnalytics order = orders.get(orderId);
        if (order == null) {
            return;
        }

        order.finish();
        long makeTime = order.getTotalMakeTimeMillis();
        totalMakeMillis.addAndGet(makeTime);
        totalOrdersFinished.incrementAndGet();
    }

    public void cancelled(int orderId) {
        OrderAnalytics order = orders.get(orderId);
        if (order != null) {
            order.pause(); // just to accumulate any partial time (though we don't use it)
        }
        orderStarted.remove(orderId);
    }

    public AnalyticsResponse getAnalytics() {
        long startedCount = totalOrdersStarted.get();
        long finishedCount = totalOrdersFinished.get();

        double averageWaitTime = 0.0;
        if (startedCount > 0) {
            averageWaitTime = millisToSeconds(totalWaitMillis.get()) / startedCount;
        }

        double averageMakeTime = 0.0;
        if (finishedCount > 0) {
            averageMakeTime = millisToSeconds(totalMakeMillis.get()) / finishedCount;
        }

        double chefUtilization = calculateChefUtilization();


        return new AnalyticsResponse(
                roundOneDecimal(averageWaitTime),
                roundOneDecimal(averageMakeTime),
                roundTwoDecimals(chefUtilization),
                getMostPopularSushi(),
                getOrdersByHour(),
                0,
                "Analytics retrieved"
        );
    }

    private void addSushiCount(String sushiName) {
        ordersBySushi.computeIfAbsent(sushiName, k -> new AtomicLong()).incrementAndGet();
    }

    private void addHourCount(int hour) {
        ordersByHour.computeIfAbsent(hour, k -> new AtomicLong()).incrementAndGet();
    }

    private double calculateChefUtilization() {
        long elapsedMillis = Duration.between(serverStartTime, Instant.now()).toMillis();
        if (elapsedMillis < 1L) {
            elapsedMillis = 1L;
        }

        long totalBusyMillis = 0L;
        for (OrderAnalytics order : orders.values()) {
            totalBusyMillis += order.getTotalMakeTimeMillis();
        }

        double utilization = (double) totalBusyMillis / (elapsedMillis * CHEF_COUNT);

        if (utilization < 0.0) {
            return 0.0;
        }

        if (utilization > 1.0) {
            return 1.0;
        }

        return utilization;
    }

    private String getMostPopularSushi() {
        String mostPopularSushi = "";
        long highestCount = 0L;

        for (Map.Entry<String, AtomicLong> entry : ordersBySushi.entrySet()) {
            long count = entry.getValue().get();

            if (count > highestCount) {
                highestCount = count;
                mostPopularSushi = entry.getKey();
            }
        }

        return mostPopularSushi;
    }

    private Map<String, Integer> getOrdersByHour() {
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();

        for (int hour = 0; hour < 24; hour++) {
            AtomicLong count = ordersByHour.get(hour);

            if (count != null) {
                result.put(String.valueOf(hour), Math.toIntExact(count.get()));
            }
        }

        return result;
    }

    private double millisToSeconds(long millis) {
        return millis / 1000.0;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}