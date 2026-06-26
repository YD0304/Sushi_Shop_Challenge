package com.YD0304.sushi_shop.service;

import com.YD0304.sushi_shop.dto.OrderStatusResponse;
import com.YD0304.sushi_shop.entity.Status;
import com.YD0304.sushi_shop.entity.Sushi;
import com.YD0304.sushi_shop.entity.SushiOrder;
import com.YD0304.sushi_shop.repository.StatusRepository;
import com.YD0304.sushi_shop.repository.SushiOrderRepository;
import com.YD0304.sushi_shop.repository.SushiRepository;
import com.YD0304.sushi_shop.service.SushiOrderService;
import com.YD0304.sushi_shop.service.AnalyticsService;
import com.YD0304.sushi_shop.service.OrderScheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderSchedulerTest {
 
    @Mock private SushiOrderService sushiOrderService;
 
    @Mock
    private OrderScheduler scheduler;
 
    @BeforeEach
    void setUp() {
        scheduler = new OrderScheduler();
    }
 
    @AfterEach
    void shutdown() {
        scheduler.shutdown();
    }

    @Test
    void getTimeSpent_invalidOrderId(){
        assertEquals(0, scheduler.getTimeSpent(999));
    }
 
    @Test
    void getTimeSpent_Success() {
        scheduler.incrementTimeSpent(5);
        scheduler.incrementTimeSpent(5);
        assertEquals(2, scheduler.getTimeSpent(5));
    }
 
    @Test
    void incrementTimeSpent_Success() {
        scheduler.incrementTimeSpent(1);
        scheduler.incrementTimeSpent(1);
        scheduler.incrementTimeSpent(2);
 
        assertEquals(2, scheduler.getTimeSpent(1));
        assertEquals(1, scheduler.getTimeSpent(2));
    }

    @Test
    void enqueue(){
        scheduler.enqueue(1, 0, sushiOrderService);

        verify(sushiOrderService, timeout(1000)).processSushiOrder(1);
    }

    @Test
    void enqueue_replace(){

        scheduler.enqueue(1, 1, sushiOrderService);
        scheduler.enqueue(1, 0, sushiOrderService);

        verify(sushiOrderService, timeout(1000).atLeastOnce()).processSushiOrder(1);
    }

    @Test
    void stop(){
        scheduler.enqueue(1, 1, sushiOrderService);
        scheduler.cancel(1);

        verify(sushiOrderService, after(500).atMostOnce())
        .processSushiOrder(1);
}


    @Test
    void taskFinished(){
        OrderTask task = new OrderTask(1, 1, 3, sushiOrderService, scheduler);
        scheduler.enqueue(1, 1, sushiOrderService);
        scheduler.taskFinished(1, task);

        verify(sushiOrderService, after(500).atMostOnce())
        .processSushiOrder(1);

    }

    
    


}