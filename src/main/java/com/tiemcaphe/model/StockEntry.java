package com.tiemcaphe.model;

import java.time.LocalDateTime;

public record StockEntry(long id, long ingredientId, String ingredientName, double quantity, StockEntryType type,
                         String note, String byName, Long orderId, LocalDateTime createdAt) {
}
