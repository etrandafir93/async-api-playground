package com.etrandafir.asyncapi.inventoryservice;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.etrandafir.asyncapi.contracts.events.StockUnavailable;
import com.etrandafir.asyncapi.contracts.events.StockUpdated;

import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@Component
class StockUpdatePublisher {

    private final StreamBridge streamBridge;

    StockUpdatePublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
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
}
