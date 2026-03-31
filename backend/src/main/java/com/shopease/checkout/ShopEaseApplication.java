package com.shopease.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ShopEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopEaseApplication.class, args);
    }
}
