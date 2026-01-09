package com.etrandafir.asyncapi.orderservice;

public record StockUnavailable(
    Sku sku,
    int availableQuantity,
    String orderId,
    int quantityRequested
) {
}
