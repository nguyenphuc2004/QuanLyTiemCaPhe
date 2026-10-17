package com.tiemcaphe.model;

/** Doanh thu theo kỳ. {@code key} là 'yyyy-MM-dd' (theo ngày) hoặc 'yyyy-MM' (theo tháng). */
public record DailyRevenue(String key, int orders, long revenue) {
}
