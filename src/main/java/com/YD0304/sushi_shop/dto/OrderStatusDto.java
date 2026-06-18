package com.YD0304.sushi_shop.dto;

public class OrderStatusDto {
    private int orderId;
    private int timeSpent;
    
    public OrderStatusDto() {
    }
    
    public OrderStatusDto(int orderId, int timeSpent) {
        this.orderId = orderId;
        this.timeSpent = timeSpent;
    }
    
    public int getOrderId() {
        return this.orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getTimeSpent() {
        return this.timeSpent;
    }
    
    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }
}

