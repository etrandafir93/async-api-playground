package com.etrandafir.asyncapi.orderservice.events;

public record StockUnavailable(
    String sku,
    int availableQuantity,
    String orderId,
    int quantityRequested
) {
}
