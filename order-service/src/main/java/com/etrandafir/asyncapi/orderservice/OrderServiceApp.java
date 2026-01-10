package com.etrandafir.asyncapi.orderservice;

import com.etrandafir.asyncapi.contracts.events.StockUnavailable;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
class OrderServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApp.class, args);
    }

    @Bean
    @AsyncListener(
        operation = @AsyncOperation(
            channelName = "stock-unavailable",
            description = "Processes stock unavailable events and cancels the order",
            payloadType = StockUnavailable.class,
            message = @AsyncMessage(
                name = "StockUnavailableMessage",
                description = "Message indicating that stock is unavailable for an order",
                contentType = "text/xml"
            )
        )
    )
    @KafkaAsyncOperationBinding
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
