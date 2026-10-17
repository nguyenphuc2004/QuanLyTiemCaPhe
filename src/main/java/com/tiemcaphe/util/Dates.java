package com.tiemcaphe.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Chuyển đổi ngày giờ giữa CSDL (TEXT) và giao diện. */
public final class Dates {

    public static final DateTimeFormatter DB = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DISPLAY_SHORT = DateTimeFormatter.ofPattern("dd/MM");
    public static final DateTimeFormatter DAY_KEY = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter CODE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private Dates() {
    }

    public static String toDb(LocalDateTime t) {
        return t == null ? null : t.withNano(0).format(DB);
    }

    public static String nowDb() {
        return toDb(LocalDateTime.now());
    }

    public static LocalDateTime fromDb(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s, DB);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(s);
            } catch (DateTimeParseException e2) {
                return LocalDate.parse(s.substring(0, Math.min(10, s.length()))).atStartOfDay();
            }
        }
    }

    /** Mốc đầu ngày, dùng cho điều kiện ngay_tao &gt;= ? */
    public static String dayStart(LocalDate d) {
        return d.atStartOfDay().format(DB);
    }

    /** Mốc đầu ngày hôm sau, dùng cho điều kiện ngay_tao &lt; ? */
    public static String dayEnd(LocalDate d) {
        return d.plusDays(1).atStartOfDay().format(DB);
    }

    public static String display(LocalDateTime t) {
        return t == null ? "" : t.format(DISPLAY);
    }

    public static String display(LocalDate d) {
        return d == null ? "" : d.format(DISPLAY_DATE);
    }

    /** 'yyyy-MM-dd' → 'dd/MM', 'yyyy-MM' → 'MM/yyyy' (nhãn trục biểu đồ). */
    public static String keyLabel(String key) {
        if (key == null) {
            return "";
        }
        if (key.length() == 10) {
            return LocalDate.parse(key).format(DISPLAY_SHORT);
        }
        if (key.length() == 7) {
            return key.substring(5) + "/" + key.substring(0, 4);
        }
        return key;
    }
}
