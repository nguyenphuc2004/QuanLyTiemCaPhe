package com.tiemcaphe.model;

import java.time.LocalDateTime;

public record Order(long id, String code, Long accountId, String staffName, String tableName, String note,
                    long subtotal, long discount, long total, long paid, long change,
                    PaymentMethod method, OrderStatus status, String cancelReason, LocalDateTime createdAt) {

    public boolean isCancelled() {
        return status == OrderStatus.DA_HUY;
    }
}
