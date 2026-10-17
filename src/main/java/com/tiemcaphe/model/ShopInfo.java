package com.tiemcaphe.model;

/** Thông tin quán in trên hóa đơn và hiển thị trên tiêu đề. */
public record ShopInfo(String name, String address, String phone, String receiptFooter) {

    public static final String KEY_NAME = "ten_quan";
    public static final String KEY_ADDRESS = "dia_chi";
    public static final String KEY_PHONE = "dien_thoai";
    public static final String KEY_FOOTER = "loi_cam_on";

    public static ShopInfo defaults() {
        return new ShopInfo("Tiệm Cà Phê", "", "", "Cảm ơn quý khách, hẹn gặp lại!");
    }
}
