package com.tiemcaphe.model;

/** Định lượng: món {@code productId} dùng {@code quantity} {@code unit} nguyên liệu {@code ingredientId}. */
public record RecipeLine(long productId, long ingredientId, String ingredientName, String unit, double quantity) {
}
