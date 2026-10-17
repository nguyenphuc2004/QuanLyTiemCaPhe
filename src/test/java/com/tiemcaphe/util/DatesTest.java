package com.tiemcaphe.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DatesTest {

    @Test
    void roundTrip() {
        LocalDateTime t = LocalDateTime.of(2026, 10, 17, 9, 5, 7);
        assertEquals("2026-10-17 09:05:07", Dates.toDb(t));
        assertEquals(t, Dates.fromDb("2026-10-17 09:05:07"));
        assertNull(Dates.fromDb(null));
        assertEquals(LocalDateTime.of(2026, 1, 2, 0, 0), Dates.fromDb("2026-01-02"));
    }

    @Test
    void ranges() {
        LocalDate d = LocalDate.of(2026, 10, 17);
        assertEquals("2026-10-17 00:00:00", Dates.dayStart(d));
        assertEquals("2026-10-18 00:00:00", Dates.dayEnd(d));
        assertEquals("17/10", Dates.keyLabel("2026-10-17"));
        assertEquals("10/2026", Dates.keyLabel("2026-10"));
    }
}
