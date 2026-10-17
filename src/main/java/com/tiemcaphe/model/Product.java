package com.tiemcaphe.model;

import java.time.LocalDateTime;

/** Một món trong thực đơn. */
public record Product(long id, String name, Long categoryId, String categoryName, long price,
                      boolean available, byte[] image, LocalDateTime updatedAt) {

    @Override
    public String toString() {
        return name;
    }
}
