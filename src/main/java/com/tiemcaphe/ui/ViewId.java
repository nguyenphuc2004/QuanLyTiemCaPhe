package com.tiemcaphe.ui;

import com.tiemcaphe.model.Account;

/** Danh sách màn hình trong khung chính, theo thứ tự hiển thị trên thanh điều hướng. */
public enum ViewId {
    DASHBOARD("Tổng quan", Icons.DASHBOARD, "/fxml/dashboard.fxml", true),
    POS("Bán hàng", Icons.CART, "/fxml/pos.fxml", false),
    MENU("Thực đơn", Icons.COFFEE, "/fxml/menu.fxml", true),
    INGREDIENTS("Nguyên liệu", Icons.BOX, "/fxml/ingredients.fxml", false),
    ORDERS("Đơn hàng", Icons.RECEIPT, "/fxml/orders.fxml", false),
    STATS("Thống kê", Icons.CHART, "/fxml/stats.fxml", true),
    ACCOUNTS("Tài khoản", Icons.PEOPLE, "/fxml/accounts.fxml", true),
    SETTINGS("Cài đặt", Icons.SETTINGS, "/fxml/settings.fxml", false);

    private final String title;
    private final String icon;
    private final String fxml;
    private final boolean adminOnly;

    ViewId(String title, String icon, String fxml, boolean adminOnly) {
        this.title = title;
        this.icon = icon;
        this.fxml = fxml;
        this.adminOnly = adminOnly;
    }

    public String title() {
        return title;
    }

    public String icon() {
        return icon;
    }

    public String fxml() {
        return fxml;
    }

    public boolean allowedFor(Account account) {
        return !adminOnly || (account != null && account.isAdmin());
    }
}
