package com.etrandafir.asyncapi.inventoryservice;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.etrandafir.asyncapi.contracts.events.OrderCreated;
import com.etrandafir.asyncapi.contracts.events.StockUnavailable;
import com.etrandafir.asyncapi.contracts.events.StockUpdated;

@SpringBootApplication
class InventoryServiceApp {

    @Autowired
    private StockUpdatePublisher stockUpdatePublisher;

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApp.class, args);
    }

    @Bean
    Consumer<OrderCreated> orderCreated() {
        return this::processOrder;
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
                    stockUpdatePublisher.publishStockUnavailable(stockUnavailable);
                } else {
                    System.out.println("Decreasing stock for product " + sku + " by " + quantity);
                    int newStock = availableQuantity - quantity;
                    StockUpdated stockUpdated = StockUpdated.newBuilder()
                        .setSku(sku)
                        .setOldStock(availableQuantity)
                        .setCurrentStock(newStock)
                        .build();
                    stockUpdatePublisher.publishStockUpdated(stockUpdated);
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
