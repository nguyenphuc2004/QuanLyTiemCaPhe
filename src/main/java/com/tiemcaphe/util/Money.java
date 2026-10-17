package com.tiemcaphe.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Định dạng tiền VNĐ (số nguyên, phân cách hàng nghìn bằng dấu chấm) và số lượng. */
public final class Money {

    private static final DecimalFormatSymbols SYMBOLS;
    private static final DecimalFormat VND;
    private static final DecimalFormat QTY;

    static {
        SYMBOLS = new DecimalFormatSymbols(Locale.ROOT);
        SYMBOLS.setGroupingSeparator('.');
        SYMBOLS.setDecimalSeparator(',');
        VND = new DecimalFormat("#,##0", SYMBOLS);
        QTY = new DecimalFormat("#,##0.####", SYMBOLS);
    }

    private Money() {
    }

    /** 25000 → "25.000 đ" */
    public static String format(long amount) {
        return formatPlain(amount) + " đ";
    }

    /** 25000 → "25.000" */
    public static String formatPlain(long amount) {
        synchronized (VND) {
            return VND.format(amount);
        }
    }

    /**
     * Chuyển chuỗi người dùng nhập thành số tiền. Chấp nhận "25.000", "25,000", "25000", "25.000 đ".
     * Chuỗi rỗng trả về 0.
     *
     * @throws NumberFormatException nếu có ký tự không hợp lệ
     */
    public static long parse(String text) {
        if (text == null) {
            return 0;
        }
        String cleaned = text.replace("đ", "").replace("₫", "").replace(".", "").replace(",", "")
                .replace(" ", "").trim();
        if (cleaned.isEmpty()) {
            return 0;
        }
        if (!cleaned.matches("-?\\d+")) {
            throw new NumberFormatException("Số tiền không hợp lệ: " + text);
        }
        return Long.parseLong(cleaned);
    }

    /** 1234.5 → "1.234,5" */
    public static String qty(double value) {
        synchronized (QTY) {
            return QTY.format(value);
        }
    }

    /**
     * Số lượng dạng chuỗi để đổ vào ô nhập (không nhóm hàng nghìn, dấu phẩy thập phân, tối đa 4 chữ số lẻ):
     * 1500 → "1500", 0.025 → "0,025".
     */
    public static String qtyPlain(double value) {
        java.math.BigDecimal bd = java.math.BigDecimal.valueOf(value).setScale(4, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return bd.toPlainString().replace('.', ',');
    }

    /**
     * Chuyển chuỗi số lượng người dùng gõ thành double. Chuỗi rỗng trả về 0.
     * Quy ước: dấu phẩy là thập phân ("1,5" = 1.5); "1.500" hay "1.234.567" là nhóm hàng nghìn;
     * "1.5" hoặc "0.025" (chỉ một dấu chấm, không theo mẫu nhóm nghìn) là thập phân.
     */
    public static double parseQty(String text) {
        if (text == null) {
            return 0;
        }
        String cleaned = text.trim().replace(" ", "");
        if (cleaned.isEmpty()) {
            return 0;
        }
        if (cleaned.matches("-?[1-9]\\d{0,2}(\\.\\d{3})+")) {
            cleaned = cleaned.replace(".", "");
        } else if (cleaned.contains(",") && !cleaned.contains(".")) {
            cleaned = cleaned.replace(',', '.');
        } else if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned = cleaned.replace(".", "").replace(',', '.');
        }
        if (!cleaned.matches("-?\\d+(\\.\\d+)?")) {
            throw new NumberFormatException("Số lượng không hợp lệ: " + text);
        }
        return Double.parseDouble(cleaned);
    }
}
