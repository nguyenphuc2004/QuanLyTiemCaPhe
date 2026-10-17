package com.tiemcaphe.model;

public record OrderLine(long id, long orderId, Long productId, String productName, long unitPrice, int quantity,
                        long lineTotal) {
}
