package com.etrandafir.asyncapi.orderservice;

import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etrandafir.asyncapi.contracts.events.OrderCreated;

import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@RestController
@RequestMapping("/api/orders")
class OrderController {

    private final StreamBridge streamBridge;

    public OrderController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @PostMapping
    @AsyncPublisher(
        operation = @AsyncOperation(
            channelName = "order-created",
            description = "Publishes an OrderCreated event when a new order is created"
        )
    )
    public ResponseEntity<OrderCreatedResponse> createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();

        OrderCreated event = OrderCreated.newBuilder()
            .setOrderId(orderId)
            .setItems(request.items())
            .build();

        streamBridge.send("orderCreated-out-0", event);

        System.out.println("Published OrderCreated event: " + event);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new OrderCreatedResponse(orderId, "Order created successfully"));
    }

    public record CreateOrderRequest(Map<String, Integer> items) {}

    public record OrderCreatedResponse(String orderId, String message) {}
}
