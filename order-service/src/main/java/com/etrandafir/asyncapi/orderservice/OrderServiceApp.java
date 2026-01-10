package com.etrandafir.asyncapi.orderservice;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.etrandafir.asyncapi.contracts.events.StockUnavailable;

@SpringBootApplication
class OrderServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApp.class, args);
    }

    @Bean
    Consumer<StockUnavailable> stockUnavailable() {
        return message -> {
            System.out.println("Received stock unavailable event: " + message);
            System.out.println("Canceling the order: " + message.getOrderId());
            System.out.println("SKU: " + message.getSku() +
                             ", Available: " + message.getAvailableQuantity() +
                             ", Requested: " + message.getQuantityRequested());
        };
    }
}
