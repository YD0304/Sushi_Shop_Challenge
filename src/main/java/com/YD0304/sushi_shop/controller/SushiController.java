// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package com.YD0304.sushi_shop.controller;

import com.YD0304.sushi_shop.dto.OrderResponse;
import com.YD0304.sushi_shop.dto.OrderStatus;
import com.YD0304.sushi_shop.dto.OrderStatusDto;
import com.YD0304.sushi_shop.dto.OrderSummary;
import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.repository.SushiRepository;
import com.YD0304.sushi_shop.service.SushiService;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SushiController {
   private final SushiService sushiService;
   private final SushiRepository sushiRepository;
   private final ParameterNamesModule parameterNamesModule;

   public SushiController(SushiService sushiService, SushiRepository sushiRepository, ParameterNamesModule parameterNamesModule) {
      this.sushiService = sushiService;
      this.sushiRepository = sushiRepository;
      this.parameterNamesModule = parameterNamesModule;
   }

   @PostMapping({"/api/orders"})
   public ResponseEntity<OrderResponse> createOrder(@RequestBody Map<String, String> payload) {
      try {
         String sushiName = (String)payload.get("sushi_name");
         SushiOrder savedOrder = this.sushiService.createSushiOrder(sushiName);
         OrderSummary summary = new OrderSummary(savedOrder);
         return ResponseEntity.status(HttpStatus.CREATED).body(new OrderResponse(summary, 0, "Order created"));
      } catch (NoSuchElementException var5) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OrderResponse((OrderSummary)null, 404, "Sushi not found"));
      } catch (Exception var6) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new OrderResponse((OrderSummary)null, 500, "Internal error"));
      }
   }

   @DeleteMapping({"/api/orders/{orderId}"})
   public ResponseEntity<OrderStatus> cancelOrder(@PathVariable Integer orderId) {
      try {
         sushiService.cancelSushiOrder(orderId);
         return ResponseEntity.ok(new OrderStatus(0, "Order cancelled"));
      } catch (NoSuchElementException var3) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OrderStatus(404, "Order not found"));
      } catch (IllegalStateException e) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new OrderStatus(400, e.getMessage()));
      } catch (Exception var5) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new OrderStatus(500, "Internal error"));
      }
   }

   @PutMapping({"/api/orders/{orderId}/pause"})
   public ResponseEntity<OrderStatus> pauseOrder(@PathVariable Integer orderId) {
      try {
         sushiService.pauseSushiOrder(orderId);
         return ResponseEntity.ok(new OrderStatus(0, "Order paused"));
      } catch (NoSuchElementException var3) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OrderStatus(404, "Order not found"));
      } catch (IllegalStateException e) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new OrderStatus(400, e.getMessage()));
      } catch (Exception var5) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new OrderStatus(500, "Internal error"));
      }
   }

   @PutMapping({"/api/orders/{orderId}/resume"})
   public ResponseEntity<OrderStatus> resumeOrder(@PathVariable Integer orderId) {
      try {
         sushiService.resumeSushiOrder(orderId);
         return ResponseEntity.ok(new OrderStatus(0, "Order resumed"));
      } catch (NoSuchElementException var3) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OrderStatus(404, "Order not found"));
      } catch (IllegalStateException e) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new OrderStatus(400, e.getMessage()));
      } catch (Exception var5) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new OrderStatus(500, "Internal error"));
      }
   }

   @GetMapping("/api/orders/status")
   public ResponseEntity<Map<String, java.util.List<OrderStatusDto>>> getOrdersByStatus() {
      return ResponseEntity.ok(sushiService.getOrdersByStatus());
   }

   @GetMapping({"/test"})
   public String test() {
      return "TEST!";
   }
}
