package com.etrandafir.asyncapi.orderservice;

import java.util.Map;

public record OrderCreated(
    String orderId,
    Map<Sku, Integer> items
) {
}
