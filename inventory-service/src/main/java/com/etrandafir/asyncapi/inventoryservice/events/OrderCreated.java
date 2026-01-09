package com.etrandafir.asyncapi.inventoryservice.events;

import java.util.Map;

public record OrderCreated(
    String orderId,
    Map<String, Integer> items
) {
}
