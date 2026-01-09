package com.etrandafir.asyncapi.inventoryservice;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Consumer<OrderCreated> orderCreated(InventoryService inventoryService) {
        return inventoryService::processOrder;
    }

}
