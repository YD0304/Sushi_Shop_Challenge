package com.YD0304.sushi_shop.dto;

import com.YD0304.sushi_shop.entity.Status;
import com.YD0304.sushi_shop.entity.SushiOrder;

public class OrderResponse {
    private OrderSummary order;
    private int code;
    private String msg;

    public OrderResponse(OrderSummary order, int code, String msg) {
        this.order = order;
        this.code = code;
        this.msg = msg;
    }

    // Getters for all fields
    public OrderSummary getOrder() { return order; }
    public int getCode() { return code; }
    public String getMsg() { return msg; }
}