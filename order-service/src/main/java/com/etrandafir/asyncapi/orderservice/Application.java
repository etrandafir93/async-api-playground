package com.etrandafir.asyncapi.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Consumer<StockUnavailable> stockUnavailable() {
        return message -> {
            System.out.println("Received stock unavailable event: " + message);
            System.out.println("Canceling the order: " + message.orderId());
            System.out.println("SKU: " + message.sku().value() +
                             ", Available: " + message.availableQuantity() +
                             ", Requested: " + message.quantityRequested());
        };
    }
}
