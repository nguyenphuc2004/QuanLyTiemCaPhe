package com.tiemcaphe.ui;

/** Controller của một màn hình trong khung chính; được gọi mỗi lần màn hình hiển thị để làm mới dữ liệu. */
public interface View {

    default void onShow() {
    }
}
