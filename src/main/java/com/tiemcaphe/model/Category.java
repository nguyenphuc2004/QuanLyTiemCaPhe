package com.tiemcaphe.model;

public record Category(long id, String name, int sortOrder) {

    @Override
    public String toString() {
        return name;
    }
}
