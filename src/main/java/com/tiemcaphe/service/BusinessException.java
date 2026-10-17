package com.tiemcaphe.service;

/** Lỗi nghiệp vụ có thông báo hiển thị trực tiếp cho người dùng. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
