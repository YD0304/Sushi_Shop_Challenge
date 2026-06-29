package com.YD0304.sushi_shop.service;

import java.util.concurrent.RejectedExecutionException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
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

    @Test
    void shutdownTest(){
    scheduler.shutdown();

    // After shutdown, enqueue should reject new tasks
    assertThrows(RejectedExecutionException.class, () -> 
        scheduler.enqueue(1, 1, sushiOrderService)
    );
}
}

    
