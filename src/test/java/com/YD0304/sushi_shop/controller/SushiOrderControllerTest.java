package com.YD0304.sushi_shop.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.List;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;



import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.mockito.Mockito.doNothing;



import com.YD0304.sushi_shop.dto.OrderStatusResponse;
import com.YD0304.sushi_shop.entity.Status;
import com.YD0304.sushi_shop.entity.Sushi;
import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.repository.StatusRepository;
import com.YD0304.sushi_shop.repository.SushiOrderRepository;
import com.YD0304.sushi_shop.repository.SushiRepository;
import com.YD0304.sushi_shop.service.SushiOrderService;


@WebMvcTest(SushiOrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SushiOrderService service;

    @MockitoBean
private SushiRepository sushiRepository;

@MockitoBean
private StatusRepository statusRepository;

    @Test
void createOrder_ValidRequest_ReturnsCreated() throws Exception {
    // 1. Arrange
    String sushiName = "California Roll";
    int timeToMake = 30;

    // Create Status and Sushi
    Status status = new Status("created");
    status.setId(1);

    Sushi sushi = new Sushi(sushiName, timeToMake);
    sushi.setId(1);

    // Create SushiOrder
    SushiOrder mockOrder = new SushiOrder(status, sushi);
    mockOrder.setId(1);  

    given(service.createSushiOrder(sushiName)).willReturn(mockOrder);

    Map<String, String> payload = Map.of("sushi_name", sushiName);

    // Act Assert
    mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.order.id").value(1))            // order ID
            .andExpect(jsonPath("$.order.statusId").value(1))       // status ID
            .andExpect(jsonPath("$.order.sushiId").value(1))      // sushi ID
            .andExpect(jsonPath("$.order.createdAt").exists())      // timestamp
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.msg").value("Order created"));   // msg

}

@Test
void cancelOrder_Success() throws Exception {
    Integer orderId = 1;
    doNothing().when(service).cancelSushiOrder(orderId); // service void

    mockMvc.perform(delete("/api/orders/{orderId}", orderId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.msg").value("Order cancelled"));
}

@Test
void pauseOrder_Success() throws Exception {
    Integer orderId = 1;
    doNothing().when(service).pauseSushiOrder(orderId);

    mockMvc.perform(put("/api/orders/{orderId}/pause", orderId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.msg").value("Order paused"));

    verify(service, times(1)).pauseSushiOrder(orderId);
}

@Test
void resumeOrder_Success() throws Exception {
    Integer orderId = 1;
    doNothing().when(service).resumeSushiOrder(orderId);

    mockMvc.perform(put("/api/orders/{orderId}/resume", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.msg").value("Order resumed"));

}
@Test
void getOrdersByStatus_Success() throws Exception {
    // 1. Arrange
    List<OrderStatusResponse> inProgressOrders = List.of(
        new OrderStatusResponse(1, 30)
    );
    List<OrderStatusResponse> completedOrders = List.of(
        new OrderStatusResponse(2, 0)
    );

    Map<String, List<OrderStatusResponse>> mockResponse = Map.of(
        "in-progress", inProgressOrders,
        "completed", completedOrders
    );

    given(service.getOrdersGroupedByStatus()).willReturn(mockResponse);

    // 2. Act & 3. Assert
    mockMvc.perform(get("/api/orders/status"))
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$['in-progress']").isArray())
            .andExpect(jsonPath("$['in-progress'].length()").value(1))
            .andExpect(jsonPath("$['in-progress'][0].orderId").value(1))    
            .andExpect(jsonPath("$['in-progress'][0].timeSpent").value(30))  
            .andExpect(jsonPath("$.completed").isArray())
            .andExpect(jsonPath("$.completed.length()").value(1))
            .andExpect(jsonPath("$.completed[0].orderId").value(2))          
            .andExpect(jsonPath("$.completed[0].timeSpent").value(0));     

    verify(service, times(1)).getOrdersGroupedByStatus();
}

    // @GetMapping("/api/orders/analytics")
    // public ResponseEntity<AnalyticsResponse> getAnalytics() {
    //     return ResponseEntity.ok(sushiService.getAnalytics());
    // }

}
