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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


/*Arrange: Set up the test data, create objects, 
mock dependencies, prepare the environment (SUT - System Under Test).

Act: Execute the specific method/behavior being tested.

Assert: Verify the expected results against the actual 
results using assertions (e.g., assertEquals, assertTrue, assertThrows). */

@ExtendWith(MockitoExtension.class)
class SushiOrderServiceTest {

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private SushiRepository sushiRepository;

    @Mock
    private SushiOrderRepository sushiOrderRepository;

    @Mock
    private OrderScheduler orderScheduler;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private SushiOrderService sushiOrderService;

    //Helper functions
    private Sushi createSushi(String name, int timeToMake) {
    Sushi sushi = new Sushi();
    sushi.setName(name);
    sushi.setTimeToMake(timeToMake);
    return sushi;
}

private Status createStatus(String name){
    Status status = new Status();
        status.setName(name);
        return status;
}

private SushiOrder createOrder(int id, Sushi sushi, Status status){
    SushiOrder order = new SushiOrder();
        order.setId(id);
        order.setSushi(sushi);
        order.setStatus(status);
         order.setCreatedAt(Instant.now());
        return order;

}

    @Test
    void createSushiOrderTest_Success() {

        // Arrange
       Sushi sushi = createSushi("California Roll", 30);

        Status status = createStatus("created");

        SushiOrder savedOrder = createOrder(1, sushi, status);


        when(sushiRepository.findByName("California Roll"))
                .thenReturn(Optional.of(sushi));

        when(statusRepository.findByName("created"))
                .thenReturn(Optional.of(status));

        when(sushiOrderRepository.save(any(SushiOrder.class)))
                .thenReturn(savedOrder);

        // Act
        SushiOrder createdOrder =
                sushiOrderService.createSushiOrder("California Roll");

        // Assert
        assertNotNull(createdOrder);
        Assertions.assertEquals("California Roll", createdOrder.getSushi().getName());
        Assertions.assertEquals("created", createdOrder.getStatus().getName());

        verify(sushiRepository).findByName("California Roll");
        verify(statusRepository).findByName("created");

        verify(sushiOrderRepository).save(any(SushiOrder.class));

       verify(analyticsService).created(
    anyInt(),
    eq("California Roll"),
    any(Instant.class)
);

verify(orderScheduler).enqueue(
    anyInt(),
    eq(1),
    any(SushiOrderService.class)
);
    }

    @Test
void createSushiOrder_Fail_SushiNotFound() {
    when(sushiRepository.findByName("unknown sushi"))
            .thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sushiOrderService.createSushiOrder("unknown sushi")
    );

    assertEquals("Sushi not found", exception.getMessage());
    verifyNoInteractions(statusRepository, sushiOrderRepository, analyticsService, orderScheduler);
}

/*
similary
createSushiOrder_StatusNotFound()
createSushiOrder_OrderNotFound()
*/


    @Test
    void canProcess(){
         Sushi sushi = new Sushi();
        Status status = new Status();
        SushiOrder order = createOrder(1, sushi, status);

        assertEquals("created", order.getStatus().getName());
    }

@Test
    void changeSushiOrderStatus_Success() {
        Sushi sushi = createSushi("California Roll", 10);
        Status create = createStatus("created");
        Status inProgress = createStatus("in-progress");
        SushiOrder order = createOrder(20, sushi, create);
 
        when(sushiOrderRepository.findById(20)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("in-progress")).thenReturn(Optional.of(inProgress));
        when(sushiOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 
        SushiOrder changedOrder = sushiOrderService.changeSushiOrderStatus(20, "in-progress");
 
        assertEquals("in-progress", changedOrder.getStatus().getName());
    }

@Test
    void changeSushiOrderStatus_Fail_orderNotFound(){
        when(sushiOrderRepository.findById(999)).thenReturn(Optional.empty());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> sushiOrderService.cancelSushiOrder(999));
    }

/*
changeSushiOrderStatus_Fail_statusNotFound()
*/

// in-progress -> cancel
@Test
void cancelSushiOrder_Success(){
    Sushi sushi = createSushi("California Roll", 30);
        Status inProgress = createStatus("in-progress");
        Status cancel = createStatus("cancelled");
        SushiOrder order = createOrder(1, sushi, inProgress);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("cancelled")).thenReturn(Optional.of(cancel));
        when (sushiOrderRepository.save(any())).thenReturn(order);

        sushiOrderService.cancelSushiOrder(1);
 
        verify(orderScheduler).cancel(1);
        verify(analyticsService).cancelled(1);
        verify(statusRepository).findByName("cancelled");
}

//similary create -> cancel
@Test
void cancelSushiOrder_Success_FromCreate(){
    Sushi sushi = createSushi("California Roll", 30);
        Status created = createStatus("created");
        Status cancel = createStatus("cancelled");
        SushiOrder order = createOrder(1, sushi, created);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("cancelled")).thenReturn(Optional.of(cancel));
        when (sushiOrderRepository.save(any())).thenReturn(order);

        sushiOrderService.cancelSushiOrder(1);
 
        verify(orderScheduler).cancel(1);
        verify(analyticsService).cancelled(1);
        verify(statusRepository).findByName("cancelled");

}

// finish -> cancel Fail
@Test
void cancelSushiOrder_Fail_FromFinish(){
    Sushi sushi = createSushi("California Roll", 30);
        Status finish = createStatus("finished");
        Status cancel = createStatus("cancelled");
        SushiOrder order = createOrder(1, sushi, finish);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("cancelled")).thenReturn(Optional.of(cancel));
        when (sushiOrderRepository.save(any())).thenReturn(order);

        IllegalStateException e = assertThrows(IllegalStateException.class, ()-> sushiOrderService.cancelSushiOrder(1));
 
        assertEquals("finished", order.getStatus().getName());
        verifyNoInteractions(orderScheduler, analyticsService);

}

// similary cancelled -> cancel Fail


// invalid order id Fail
@Test
void cancelSushiOrder_Fail_OrderNotFound(){
    when(sushiOrderRepository.findById(999)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, ()->sushiOrderService.cancelSushiOrder(999));
}

@Test
void pauseSushiOrder_Success(){
    Sushi sushi = createSushi("California Roll", 30);
        Status inProgress = createStatus("in-progress");
        Status pause = createStatus("paused");
        SushiOrder order = createOrder(1, sushi, inProgress);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("paused")).thenReturn(Optional.of(pause));
        when(sushiOrderRepository.save(any())).thenReturn (order);

        sushiOrderService.pauseSushiOrder(1);
 
        verify(orderScheduler).pause(1);
        verify(analyticsService).paused(1);

}


// finish -> pause Fail
@Test
void pauseSushiOrder_Fail_FromFinish(){

     Sushi sushi = createSushi("California Roll", 30);
        Status finish = createStatus("finished");
        Status pause = createStatus("paused");
        SushiOrder order = createOrder(1, sushi, finish);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("paused")).thenReturn(Optional.of(pause));
        when(sushiOrderRepository.save(any())).thenReturn (order);

        assertThrows(IllegalStateException.class, () -> sushiOrderService.pauseSushiOrder(1));
 
        verifyNoInteractions(orderScheduler, analyticsService);
    
}
//created -> pause Fail
@Test
void pauseSushiOrder_Fail_FromCreate(){

     Sushi sushi = createSushi("California Roll", 30);
        Status create = createStatus("created");
        Status pause = createStatus("paused");
        SushiOrder order = createOrder(1, sushi, create);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("paused")).thenReturn(Optional.of(pause));
        when(sushiOrderRepository.save(any())).thenReturn (order);

        assertThrows(IllegalStateException.class, () -> sushiOrderService.pauseSushiOrder(1));
 
        verifyNoInteractions(orderScheduler, analyticsService);
    
}

//order not found

@Test
void pauseSushiOrder_Fail_OrderNotFound(){
    when(sushiOrderRepository.findById(999)).thenReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, ()-> sushiOrderService.pauseSushiOrder(999));
}

@Test
void resumeSushiOrder_Success(){
    Sushi sushi = createSushi("California Roll", 30);
        Status recreate = createStatus("created");
        Status pause = createStatus("paused");
        SushiOrder order = createOrder(1, sushi, pause);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("created")).thenReturn(Optional.of(recreate));
        when(sushiOrderRepository.save(any())).thenReturn(order); 

        sushiOrderService.resumeSushiOrder(1);
 
        //re-enqued with higher priority 0
        verify(orderScheduler).enqueue(1, 0, sushiOrderService);
}

//status other than paused -> pause Fail
@Test
void resumeSushiOrder_Fail_FromInProgress(){
    Sushi sushi = createSushi("California Roll", 30);
        Status progress = createStatus("in-progress");
        Status pause = createStatus("paused");
        SushiOrder order = createOrder(1, sushi, pause);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("created")).thenReturn(Optional.of(progress));

        assertThrows(IllegalStateException.class, () -> sushiOrderService.resumeSushiOrder(1));

        verifyNoInteractions(analyticsService, orderScheduler);
}

@Test
void getOrdersGroupedByStatus_Success(){
    Sushi sushi1 = createSushi("California Roll", 30);
    Sushi sushi2 = createSushi("Dragon eye", 50);

    Status created = createStatus("created");
    Status inProgress = createStatus("in-progress");

    SushiOrder firstOrder = createOrder(1, sushi1, created);
    SushiOrder secondOrder = createOrder(2, sushi2, inProgress);

    when(sushiOrderRepository.findAll()).thenReturn(List.of(firstOrder, secondOrder));
    
    when(orderScheduler.getTimeSpent(1)).thenReturn(0); //just got created
    when(orderScheduler.getTimeSpent(2)).thenReturn(50);
     //Act
    Map<String, List<OrderStatusResponse>> orderList =
                sushiOrderService.getOrdersGroupedByStatus();

    assertNotNull(orderList);
    assertEquals(2, orderList.size());
    assertTrue(orderList.containsKey("created"));
    assertTrue(orderList.containsKey("in-progress"));
}

@Test
void getOrdersGroupedByStatus_Sucess_EmptyRepo(){
     when(sushiOrderRepository.findAll()).thenReturn(List.of());
     Map<String, List<OrderStatusResponse>> orderList =
                sushiOrderService.getOrdersGroupedByStatus();
    assertNotNull(orderList);
    assertTrue(orderList.isEmpty());
}


@Test
void processSushiOrder_normalCompletion() throws Exception {
    // Arrange
    Sushi sushi = new Sushi();
    sushi.setTimeToMake(3); // cooking time = 3 seconds
    Status created = new Status("created");
    Status inProgress = new Status("in-progress");
    Status finished = new Status("finished");
    SushiOrder order = new SushiOrder();
    order.setId(1);
    order.setSushi(sushi);
    order.setStatus(created);

    // Stub repository to return the order
    when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
    when(statusRepository.findByName("in-progress")).thenReturn(Optional.of(inProgress));
    when(statusRepository.findByName("finished")).thenReturn(Optional.of(finished));

    // Stub timeSpent: initially 0, then 1, 2, 3 (so loop runs 3 times)
    when(orderScheduler.getTimeSpent(1))
        .thenReturn(0, 1, 2, 3);
    // Stub incrementTimeSpent to do nothing (or we can just verify it was called)
    doNothing().when(orderScheduler).incrementTimeSpent(1);

    // Act
    sushiOrderService.processSushiOrder(1);

    // Assert
    verify(analyticsService).started(1);
    verify(analyticsService).finished(1);
    verify(orderScheduler, times(3)).incrementTimeSpent(1);
    verify(sushiOrderRepository, atLeast(1)).save(any()); // status changed to finished
}

}