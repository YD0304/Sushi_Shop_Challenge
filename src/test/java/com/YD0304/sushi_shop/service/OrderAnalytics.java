// package com.YD0304.sushi_shop.service;

// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.YD0304.sushi_shop.repository.StatusRepository;
// import com.YD0304.sushi_shop.repository.SushiOrderRepository;
// import com.YD0304.sushi_shop.repository.SushiRepository;

// @ExtendWith(MockitoExtension.class)
// public class OrderAnalytics {

//     @Mock
//     private StatusRepository statusRepository;

//     @Mock
//     private SushiRepository sushiRepository;

//     @Mock
//     private SushiOrderRepository sushiOrderRepository;

//     @Mock
//     private OrderScheduler orderScheduler;

//     @Mock
//     private AnalyticsService analyticsService;

//     @InjectMocks
//     private SushiOrderService sushiOrderService;

//     @BeforeAll
//     int numberOfThreads = 3;
//     ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
//     CountDownLatch latch = new CountDownLatch(numberOfThreads);
    
    
//     @AfterEach
    
    
    
//     @Test
//     void getOrderId(){

//     }
    
// }
