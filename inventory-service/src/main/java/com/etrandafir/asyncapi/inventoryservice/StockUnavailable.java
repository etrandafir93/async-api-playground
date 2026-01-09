package com.etrandafir.asyncapi.inventoryservice;

public record StockUnavailable(
    Sku sku,
    int availableQuantity,
    String orderId,
    int quantityRequested
) {
}
