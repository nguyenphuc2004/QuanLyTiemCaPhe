package com.tiemcaphe.model;

public enum OrderStatus {
    DA_THANH_TOAN("Đã thanh toán"),
    DA_HUY("Đã hủy");

    private final String label;

    OrderStatus(String label) {
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
