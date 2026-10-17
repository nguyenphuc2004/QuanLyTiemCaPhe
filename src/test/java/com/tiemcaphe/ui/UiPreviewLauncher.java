package com.tiemcaphe.ui;

/** Lớp khởi động cho UiPreview khi chạy từ classpath (không kế thừa Application). */
public final class UiPreviewLauncher {

    private UiPreviewLauncher() {
    }

    public static void main(String[] args) {
        UiPreview.main(args);
    }
}
