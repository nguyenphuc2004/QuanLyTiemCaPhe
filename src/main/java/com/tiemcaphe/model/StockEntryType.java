package com.tiemcaphe.model;

public enum StockEntryType {
    NHAP("Nhập kho"),
    XUAT("Xuất kho"),
    DIEU_CHINH("Điều chỉnh"),
    BAN_HANG("Bán hàng"),
    HUY_DON("Hủy đơn");

    private final String label;

    StockEntryType(String label) {
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
