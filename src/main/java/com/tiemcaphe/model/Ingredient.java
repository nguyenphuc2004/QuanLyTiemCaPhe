package com.tiemcaphe.model;

import java.time.LocalDateTime;

public record Ingredient(long id, String name, double quantity, String unit, double warnThreshold,
                         byte[] image, LocalDateTime updatedAt) {

    /** Sắp hết: có đặt ngưỡng và tồn kho đã xuống bằng hoặc dưới ngưỡng. */
    public boolean isLow() {
        return warnThreshold > 0 && quantity <= warnThreshold;
    }

    @Override
    public String toString() {
        return name;
    }
}
