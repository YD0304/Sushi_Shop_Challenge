package com.YD0304.sushi_shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderAnalyticsTest {

    private OrderAnalytics analytic;

    @BeforeEach
    void setup() {
        analytic = new OrderAnalytics(1);
    }

    @Test
    void pause_resume_accumulatesTime() throws InterruptedException {

        analytic.start();
        Thread.sleep(2000);

        analytic.pause();
        analytic.start();

        Thread.sleep(2000);

        analytic.finish();

        assertTrue(analytic.getTotalMakeTimeMillis() >= 4000);
    }

    @Test
    void pause_create(){
        OrderAnalytics a = new OrderAnalytics(1);
        a.pause();

        assertEquals(0, a.getTotalMakeTimeMillis());

    }

    @Test
    void getTotalMakeTimeMillis() throws InterruptedException{
        analytic.start();
        Thread.sleep(1000);
        long time = analytic.getTotalMakeTimeMillis();
        assertTrue(time >= 1000);
    }

    
}