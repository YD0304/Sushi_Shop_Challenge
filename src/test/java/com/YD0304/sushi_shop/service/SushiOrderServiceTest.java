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
    void createSushiOrderTestSuccess() {

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
void createSushiOrder_SushiNotFound() {
    when(sushiRepository.findByName("unknown sushi"))
            .thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sushiOrderService.createSushiOrder("unknown sushi")
    );

    assertEquals("Sushi not found", exception.getMessage());
}

/*
createSushiOrder_StatusNotFound()
createSushiOrder_OrderNotFound()
*/

//     @Test
//     void processSushiOrder(){
//         Sushi sushi = Sushi();
//         Status status = new Status();
//         SushiOrder order = createOrder(1, sushi, status);

//         when(sushiOrderRepository.findById(1))
//             .thenReturn(Optional.of(order));  


//         verify(analyticsService).created(
//     anyInt(),
//     eq("California Roll"),
//     any(Instant.class)
// );
//     }

//     @Test
//     void canProcess(){
//          Sushi sushi = new Sushi();
//         Status status = new Status();
//         SushiOrder order = createOrder(1, sushi, status);

//         assertEquals("created", order.getStatus().getName());
//     }

@Test
    void changeSushiOrderStatus_validTransition_updatesStatus() {
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
void cancelSushiOrder(){
    Sushi sushi = createSushi("California Roll", 30);
        Status inProgress = createStatus("in-progress");
        Status cancel = createStatus("cancelled");
        SushiOrder order = createOrder(1, sushi, inProgress);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("cancelled")).thenReturn(Optional.of(cancel));

        sushiOrderService.cancelSushiOrder(1);
 
        verify(orderScheduler).cancel(1);
        verify(analyticsService).cancelled(1);

}

@Test
void pauseSushiOrder(){
    Sushi sushi = createSushi("California Roll", 30);
        Status inProgress = createStatus("in-progress");
        Status pause = createStatus("paused");
        SushiOrder order = createOrder(1, sushi, inProgress);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("paused")).thenReturn(Optional.of(pause));

        sushiOrderService.pauseSushiOrder(1);
 
        verify(orderScheduler).pause(1);
        verify(analyticsService).paused(1);

}

@Test
void resumeSushiOrder(){

    Sushi sushi = createSushi("California Roll", 30);
        Status recreate = createStatus("created");
        Status pause = createStatus("paused");
        SushiOrder order = createOrder(1, sushi, pause);

        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(order));
        when(statusRepository.findByName("created")).thenReturn(Optional.of(recreate));

        sushiOrderService.resumeSushiOrder(1);
 
        verify(orderScheduler).enqueue(1, 0, sushiOrderService);

}

@Test
void getOrdersGroupedByStatus(){
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


    


}


}