package com.YD0304.sushi_shop.dto;

import java.time.Instant;

import com.YD0304.sushi_shop.entity.SushiOrder;

public class OrderSummary {
    private int id;
    private int statusId;
    private int sushiId;
    private Instant createdAt;

    public OrderSummary(SushiOrder order) {
        this.id = order.getId();
        this.statusId = order.getStatus().getId();
        this.sushiId = order.getSushi().getId();
        this.createdAt = order.getCreatedAt();
    }

    public int getId() { return id; }
    public int getStatusId() { return statusId; }
    public int getSushiId() { return sushiId; }
    public Instant getCreatedAt() { return createdAt; }
}