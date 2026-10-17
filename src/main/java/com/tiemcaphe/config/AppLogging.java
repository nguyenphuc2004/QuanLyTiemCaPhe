package com.tiemcaphe.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Ghi log ra file ~/TiemCaPhe/logs/app.N.log (xoay vòng 3 file x 1MB). */
public final class AppLogging {

    private static boolean initialized;

    private AppLogging() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT [%4$s] %3$s - %5$s%6$s%n");
        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);
        try {
            Path dir = AppConfig.logDir();
            Files.createDirectories(dir);
            FileHandler fh = new FileHandler(dir.resolve("app.%g.log").toString(), 1_000_000, 3, true);
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.INFO);
            root.addHandler(fh);
        } catch (IOException e) {
            root.log(Level.WARNING, "Không ghi được log ra file: " + e.getMessage());
        }
    }
}
