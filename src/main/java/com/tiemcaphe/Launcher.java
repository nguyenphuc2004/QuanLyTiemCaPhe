package com.tiemcaphe;

/**
 * Lớp khởi động cho file jar đóng gói (không kế thừa Application để JavaFX chạy được từ classpath).
 * Dùng: java -jar tiemcaphe.jar [--demo]
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}
