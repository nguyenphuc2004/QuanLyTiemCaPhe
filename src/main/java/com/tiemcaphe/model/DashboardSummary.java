package com.tiemcaphe.model;

public record DashboardSummary(long todayRevenue, int todayOrders, long monthRevenue, int monthOrders,
                               int lowStockCount, int productCount) {
}
