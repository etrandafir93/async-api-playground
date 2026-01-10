package com.etrandafir.asyncapi.inventoryservice;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Payload;

import com.etrandafir.asyncapi.contracts.events.OrderCreated;
import com.etrandafir.asyncapi.contracts.events.StockUnavailable;
import com.etrandafir.asyncapi.contracts.events.StockUpdated;

import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@SpringBootApplication
class InventoryServiceApp {

    @Autowired
    private StreamBridge streamBridge;

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApp.class, args);
    }

    @Bean
    @AsyncListener(
        operation = @AsyncOperation(
            channelName = "order-created",
            description = "Processes order created events and checks inventory",
            payloadType = OrderCreated.class
        )
    )
    @KafkaAsyncOperationBinding
    Consumer<OrderCreated> orderCreated() {
        return this::processOrder;
    }

    @AsyncPublisher(
        operation = @AsyncOperation(
            channelName = "stock-unavailable",
            description = "Publishes stock unavailable events when requested quantity exceeds available stock"
        )
    )
    @KafkaAsyncOperationBinding
    public void publishStockUnavailable(@Payload StockUnavailable stockUnavailable) {
        streamBridge.send("stockUnavailable-out-0", stockUnavailable);
    }

    @AsyncPublisher(
        operation = @AsyncOperation(
            channelName = "stock-updated",
            description = "Publishes stock updated events when stock level changes after processing an order"
        )
    )
    @KafkaAsyncOperationBinding
    public void publishStockUpdated(@Payload StockUpdated stockUpdated) {
        streamBridge.send("stockUpdated-out-0", stockUpdated);
    }

    public void processOrder(OrderCreated message) {
        System.out.println("Received order created event: " + message);
        System.out.println("Order ID: " + message.getOrderId());

        message.getItems()
            .forEach((sku, quantity) -> {
                int availableQuantity = parseAvailableQuantity(sku);
                System.out.println("Processing SKU: " + sku + ", Requested: " + quantity + ", Available: " + availableQuantity);

                if (availableQuantity < quantity) {
                    System.out.println("Insufficient stock for " + sku + ". Publishing stock unavailable event.");
                    StockUnavailable stockUnavailable = StockUnavailable.newBuilder()
                        .setSku(sku)
                        .setAvailableQuantity(availableQuantity)
                        .setOrderId(message.getOrderId())
                        .setQuantityRequested(quantity)
                        .build();
                    publishStockUnavailable(stockUnavailable);
                } else {
                    System.out.println("Decreasing stock for product " + sku + " by " + quantity);
                    int newStock = availableQuantity - quantity;
                    StockUpdated stockUpdated = StockUpdated.newBuilder()
                        .setSku(sku)
                        .setOldStock(availableQuantity)
                        .setCurrentStock(newStock)
                        .build();
                    publishStockUpdated(stockUpdated);
                }
            });
    }

    private int parseAvailableQuantity(String skuValue) {
        try {
            String[] parts = skuValue.split("-");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Could not parse quantity from SKU: " + skuValue + ", defaulting to 0");
        }
        return 0;
    }
}
