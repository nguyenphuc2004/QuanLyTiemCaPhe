package com.tiemcaphe.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Cấu hình đường dẫn và thông tin phiên bản của ứng dụng.
 *
 * Thứ tự ưu tiên thư mục dữ liệu:
 *  1. -Dtiemcaphe.home=&lt;thư mục&gt;
 *  2. biến môi trường TIEMCAPHE_HOME
 *  3. ~/TiemCaPhe
 */
public final class AppConfig {

    public static final String APP_NAME = "Quản lý tiệm cà phê";
    public static final String VERSION = loadVersion();

    public static final String PROP_HOME = "tiemcaphe.home";
    public static final String PROP_DB = "tiemcaphe.db";
    public static final String ENV_HOME = "TIEMCAPHE_HOME";

    private AppConfig() {
    }

    public static Path homeDir() {
        String v = System.getProperty(PROP_HOME);
        if (v == null || v.isBlank()) {
            v = System.getenv(ENV_HOME);
        }
        if (v == null || v.isBlank()) {
            return Paths.get(System.getProperty("user.home"), "TiemCaPhe");
        }
        return Paths.get(v);
    }

    public static Path dbFile() {
        String v = System.getProperty(PROP_DB);
        if (v != null && !v.isBlank()) {
            return Paths.get(v);
        }
        return homeDir().resolve("tiemcaphe.db");
    }

    public static Path logDir() {
        return homeDir().resolve("logs");
    }

    public static Path backupDir() {
        return homeDir().resolve("backup");
    }

    private static String loadVersion() {
        try (InputStream in = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                String v = p.getProperty("app.version");
                if (v != null && !v.isBlank() && !v.startsWith("${")) {
                    return v;
                }
            }
        } catch (IOException ignored) {
            // bỏ qua, dùng giá trị mặc định
        }
        return "dev";
    }
}
