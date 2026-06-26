package com.YD0304.sushi_shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.YD0304.sushi_shop.dto.AnalyticsResponse;

import jakarta.inject.Inject;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {
    @Mock
    private OrderAnalytics analytic;

    @InjectMocks
    private AnalyticsService service;

    @BeforeEach
    void setup() {
        analytic = new OrderAnalytics(1);
    }

    @Test
    void getAnalytics(){

    service.created(1, "Callifornia Roll", Instant.now());
    service.created(2, "Callifornia Roll", Instant.now());
    service.created(3, "Dragon eye", Instant.now());
    service.created(4, "Dragon eye", Instant.now());
    service.created(5, "Dragon eye", Instant.now());


    AnalyticsResponse res = service.getAnalytics();

    assertEquals("Dragon eye", res.getMostPopularSushi());

    }

    @Test
    void started() throws InterruptedException{
        service.created(1, "Callifornia Roll", Instant.now());
        
        Thread.sleep(100);

        service.started(1);

        AnalyticsResponse res = service.getAnalytics();
        
        assertTrue(res.getAverageWaitTime() > 0);

    }
@Test
void finished() throws InterruptedException {

    service.created(1, "California Roll", Instant.now());
    service.started(1);
    Thread.sleep(10000);
    service.finished(1);

    AnalyticsResponse res = service.getAnalytics();

    assertTrue(res.getAverageMakeTime() > 0);
}
 
}
