package com.tiemcaphe.model;

public enum Role {
    ADMIN("Quản trị"),
    NHAN_VIEN("Nhân viên");

    private final String label;

    Role(String label) {
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
