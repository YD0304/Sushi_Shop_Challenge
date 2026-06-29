package com.YD0304.sushi_shop.service;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.YD0304.sushi_shop.dto.AnalyticsResponse;

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
    void getAnalytics() throws InterruptedException {

        service.created(1, "Callifornia Roll", Instant.now());
        service.created(2, "Callifornia Roll", Instant.now());
        service.created(3, "Dragon eye", Instant.now());
        service.created(4, "Dragon eye", Instant.now());
        service.created(5, "Dragon eye", Instant.now());

        service.started(1);
        Thread.sleep(3000);
        service.finished(1);
        service.started(2);
        Thread.sleep(3000);
        service.finished(2);

        service.started(3);
        Thread.sleep(3000);
        service.finished(3);
        
        AnalyticsResponse res = service.getAnalytics();

        assertEquals("Dragon eye", res.getMostPopularSushi());
        assertEquals(3.0, res.getAverageMakeTime());
        assertEquals(3.0, res.getAverageWaitTime());

        assertEquals(0.33, res.getChefUtilization());
    }
    @Test
void getAnalytics_EmptyData() {
    AnalyticsResponse res = service.getAnalytics();
    assertEquals(0.0, res.getAverageWaitTime());
    assertEquals(0.0, res.getAverageMakeTime());
    assertEquals(0.0, res.getChefUtilization());
    assertEquals("", res.getMostPopularSushi());
    assertTrue(res.getOrdersByHour().isEmpty());
}

    @Test
    void started() throws InterruptedException {
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

    @Test
    void paused() throws InterruptedException {
        service.created(1, "California Roll", Instant.now());
        service.started(1);
        Thread.sleep(2000);

        service.paused(1);
        Thread.sleep(3000);
        service.finished(1);
        AnalyticsResponse res = service.getAnalytics();
        assertEquals(2.0, res.getAverageMakeTime());
    }

    @Test
    void cancelled() throws InterruptedException {
        service.created(1, "California Roll", Instant.now());
        service.started(1);
        Thread.sleep(10000);
        service.cancelled(1);
        AnalyticsResponse res = service.getAnalytics();
        assertEquals(0.0, res.getAverageMakeTime());
    }
}
