package com.YD0304.sushi_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SushiShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(SushiShopApplication.class, args);
    }

}
