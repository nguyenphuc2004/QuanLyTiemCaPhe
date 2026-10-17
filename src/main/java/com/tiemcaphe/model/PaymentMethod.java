package com.tiemcaphe.model;

public enum PaymentMethod {
    TIEN_MAT("Tiền mặt"),
    CHUYEN_KHOAN("Chuyển khoản");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
