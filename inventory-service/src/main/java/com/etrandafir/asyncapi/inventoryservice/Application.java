package com.etrandafir.asyncapi.inventoryservice;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.etrandafir.asyncapi.inventoryservice.events.OrderCreated;

@SpringBootApplication
class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Consumer<OrderCreated> orderCreated(InventoryService inventoryService) {
        return inventoryService::processOrder;
    }

}
