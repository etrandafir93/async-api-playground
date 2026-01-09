package com.etrandafir.asyncapi.inventoryservice.events;

public record StockUnavailable(
    String sku,
    int availableQuantity,
    String orderId,
    int quantityRequested
) {
}
