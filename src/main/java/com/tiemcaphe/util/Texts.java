package com.tiemcaphe.util;

import java.text.Normalizer;
import java.util.Locale;

/** Tìm kiếm không phân biệt dấu tiếng Việt và hoa/thường. */
public final class Texts {

    private Texts() {
    }

    public static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }

    public static boolean contains(String haystack, String needle) {
        if (needle == null || needle.isBlank()) {
            return true;
        }
        return normalize(haystack).contains(normalize(needle));
    }

    public static String orDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }
}
