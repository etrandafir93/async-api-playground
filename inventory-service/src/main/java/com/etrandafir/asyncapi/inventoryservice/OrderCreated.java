package com.etrandafir.asyncapi.inventoryservice;

import java.util.Map;

public record OrderCreated(
    String orderId,
    Map<Sku, Integer> items
) {
}
