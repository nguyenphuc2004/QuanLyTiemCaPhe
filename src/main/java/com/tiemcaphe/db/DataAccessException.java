package com.tiemcaphe.db;

/** Lỗi truy cập CSDL (bọc SQLException để tầng trên không phải khai báo checked exception). */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
