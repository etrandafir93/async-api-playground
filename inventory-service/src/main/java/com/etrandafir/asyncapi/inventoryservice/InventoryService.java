package com.etrandafir.asyncapi.inventoryservice;

import com.etrandafir.asyncapi.inventoryservice.events.OrderCreated;
import com.etrandafir.asyncapi.inventoryservice.events.StockUnavailable;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
class InventoryService {

    private final StreamBridge streamBridge;

    public InventoryService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @AsyncListener(
        operation = @AsyncOperation(
            channelName = "order-created",
            description = "Processes order created events and validates stock availability"
        )
    )
    @KafkaAsyncOperationBinding
    public void processOrder(OrderCreated message) {
        System.out.println("Received order created event: " + message);
        System.out.println("Order ID: " + message.orderId());

        message.items()
            .forEach((sku, quantity) -> {
                int availableQuantity = parseAvailableQuantity(sku);
                System.out.println("Processing SKU: " + sku + ", Requested: " + quantity + ", Available: " + availableQuantity);

                if (availableQuantity < quantity) {
                    System.out.println("Insufficient stock for " + sku + ". Publishing stock unavailable event.");
                    StockUnavailable stockUnavailable = new StockUnavailable(sku, availableQuantity, message.orderId(), quantity);
                    publishStockUnavailable(stockUnavailable);
                } else {
                    System.out.println("Decreasing stock for product " + sku + " by " + quantity);
                }
            });
    }

    @AsyncPublisher(
        operation = @AsyncOperation(
            channelName = "stock-unavailable",
            description = "Publishes stock unavailable events when requested quantity exceeds available stock"
        )
    )
    @KafkaAsyncOperationBinding
    public void publishStockUnavailable(StockUnavailable stockUnavailable) {
        streamBridge.send("stockUnavailable-out-0", stockUnavailable);
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
