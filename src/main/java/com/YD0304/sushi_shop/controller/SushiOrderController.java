package com.YD0304.sushi_shop.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.YD0304.sushi_shop.dto.AnalyticsResponse;
import com.YD0304.sushi_shop.dto.OrderResponse;
import com.YD0304.sushi_shop.dto.OrderStatusResponse;
import com.YD0304.sushi_shop.dto.OrderSummary;
import com.YD0304.sushi_shop.dto.CodeResponse;
import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.service.SushiOrderService;

@RestController
public class SushiOrderController {

    private final SushiOrderService sushiService;

    public SushiOrderController(SushiOrderService sushiService) {
        this.sushiService = sushiService;
    }

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody Map<String, String> payload) {
        String sushiName = payload.getOrDefault("sushi_name", null);
        if (sushiName == null || sushiName.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new OrderResponse(null, 400, "sushi_name is required"));
        }
        SushiOrder savedOrder = sushiService.createSushiOrder(sushiName);
        OrderSummary summary = new OrderSummary(savedOrder);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new OrderResponse(summary, 0, "Order created"));
    }

    @DeleteMapping("/api/orders/{orderId}")
    public ResponseEntity<CodeResponse> cancelOrder(@PathVariable Integer orderId) {
        sushiService.cancelSushiOrder(orderId);
        return ResponseEntity.ok(new CodeResponse(0, "Order cancelled"));
    }

    @PutMapping("/api/orders/{orderId}/pause")
    public ResponseEntity<CodeResponse> pauseOrder(@PathVariable Integer orderId) {
        sushiService.pauseSushiOrder(orderId);
        return ResponseEntity.ok(new CodeResponse(0, "Order paused"));
    }

    @PutMapping("/api/orders/{orderId}/resume")
    public ResponseEntity<CodeResponse> resumeOrder(@PathVariable Integer orderId) {
        sushiService.resumeSushiOrder(orderId);
        return ResponseEntity.ok(new CodeResponse(0, "Order resumed"));
    }

    @GetMapping("/api/orders/status")
    public ResponseEntity<Map<String, List<OrderStatusResponse>>> getOrdersByStatus() {
        return ResponseEntity.ok(sushiService.getOrdersGroupedByStatus());
    }

    @GetMapping("/api/orders/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(sushiService.getAnalytics());
    }

    @GetMapping("/test")
    public String test() {
        return "TEST!";
    }
}