package com.tiemcaphe.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void formatsVietnameseStyle() {
        assertEquals("25.000 đ", Money.format(25_000));
        assertEquals("1.250.000", Money.formatPlain(1_250_000));
        assertEquals("0 đ", Money.format(0));
    }

    @Test
    void parsesCommonInputs() {
        assertEquals(25_000, Money.parse("25.000"));
        assertEquals(25_000, Money.parse("25,000"));
        assertEquals(25_000, Money.parse("25000"));
        assertEquals(25_000, Money.parse(" 25.000 đ "));
        assertEquals(0, Money.parse(""));
        assertEquals(0, Money.parse(null));
        assertThrows(NumberFormatException.class, () -> Money.parse("25k"));
    }

    @Test
    void quantities() {
        assertEquals("1.234,5", Money.qty(1234.5));
        assertEquals("2", Money.qty(2.0));
        assertEquals("0,025", Money.qty(0.025), "định lượng nhỏ không được làm tròn mất");
        assertEquals("1500", Money.qtyPlain(1500));
        assertEquals("0,025", Money.qtyPlain(0.025));
        assertEquals("4,975", Money.qtyPlain(4.975));
        assertEquals(1.5, Money.parseQty("1,5"));
        assertEquals(1.5, Money.parseQty("1.5"));
        assertEquals(0.025, Money.parseQty("0.025"));
        assertEquals(0.025, Money.parseQty("0,025"));
        assertEquals(1500, Money.parseQty("1.500"), "1.500 là một nghìn năm trăm như app hiển thị");
        assertEquals(1234567, Money.parseQty("1.234.567"));
        assertEquals(1234.5, Money.parseQty("1.234,5"));
        assertEquals(1500, Money.parseQty(Money.qtyPlain(1500)), "giá trị đổ vào ô nhập phải đọc lại đúng");
        assertThrows(NumberFormatException.class, () -> Money.parseQty("abc"));
        assertThrows(NumberFormatException.class, () -> Money.parseQty("1.2.3"));
    }
}
