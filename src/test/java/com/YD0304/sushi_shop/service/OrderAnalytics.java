package com.YD0304.sushi_shop.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.YD0304.sushi_shop.repository.StatusRepository;
import com.YD0304.sushi_shop.repository.SushiOrderRepository;
import com.YD0304.sushi_shop.repository.SushiRepository;

@ExtendWith(MockitoExtension.class)
public class OrderAnalytics {
    @Mock
    private OrderScheduler orderScheduler;

    @Mock
    private AnalyticsService analyticsService;

    // @InjectMocks
    // private SushiOrderService sushiOrderService;

    @BeforeEach
    void setUp() {
        orderScheduler = new OrderScheduler();
    }
    

    @AfterEach
        void shutdown() {
        orderScheduler.shutdown();
    }
    
    @Test
    void getOrderId(){

    }
    
}
