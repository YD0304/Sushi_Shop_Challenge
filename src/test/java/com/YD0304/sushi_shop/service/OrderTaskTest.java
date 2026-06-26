package com.YD0304.sushi_shop.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderTaskTest {
     @Mock private SushiOrderService sushiOrderService;
     @Mock private OrderScheduler scheduler;     

    
     
     @Test
     void compareTo_resumedOrder(){
    OrderTask t1 = new OrderTask(1, 1, 10, sushiOrderService, scheduler);
    OrderTask t2 = new OrderTask(2, 0, 5, sushiOrderService, scheduler);

    //task 2 has lower priority
    assertTrue(t1.compareTo(t2) > 0);
}

void compareTo_samePriorityUsesSequence() {
    OrderTask t1 = new OrderTask(1, 1, 1, sushiOrderService, scheduler);
    OrderTask t2 = new OrderTask(2, 1, 2, sushiOrderService, scheduler);

    //same priority FIFO
    assertTrue(t1.compareTo(t2) < 0);
}
@Test
void interrupt(){
    OrderTask task = new OrderTask(1, 1, 1, sushiOrderService, scheduler);
    assertDoesNotThrow(task::interrupt);
}

@Test
void run(){

    OrderTask task = new OrderTask(1, 1, 1, sushiOrderService, scheduler);

    task.run();

    verify(sushiOrderService).processSushiOrder(1);
    verify(scheduler).taskFinished(eq(1), any(OrderTask.class));
}
}
