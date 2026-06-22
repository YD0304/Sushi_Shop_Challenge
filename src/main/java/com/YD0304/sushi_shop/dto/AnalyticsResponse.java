package com.YD0304.sushi_shop.dto;

import java.util.Map;

public class AnalyticsResponse {

    private double averageWaitTime;
    private double averageMakeTime;
    private double chefUtilization;
    private String mostPopularSushi;
    private Map<String, Integer> ordersByHour;
    private int code;
    private String msg;

    public AnalyticsResponse(double averageWaitTime, double averageMakeTime, double chefUtilization,
            String mostPopularSushi, Map<String, Integer> ordersByHour, int code, String msg) {
        this.averageWaitTime = averageWaitTime;
        this.averageMakeTime = averageMakeTime;
        this.chefUtilization = chefUtilization;
        this.mostPopularSushi = mostPopularSushi;
        this.ordersByHour = ordersByHour;
        this.code = code;
        this.msg = msg;
    }

    public double getAverageWaitTime() {
        return averageWaitTime;
    }

    public double getAverageMakeTime() {
        return averageMakeTime;
    }

    public double getChefUtilization() {
        return chefUtilization;
    }

    public String getMostPopularSushi() {
        return mostPopularSushi;
    }

    public Map<String, Integer> getOrdersByHour() {
        return ordersByHour;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
