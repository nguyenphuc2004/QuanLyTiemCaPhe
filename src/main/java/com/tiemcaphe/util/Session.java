package com.tiemcaphe.util;

import com.tiemcaphe.model.Account;

/** Người dùng đang đăng nhập. */
public final class Session {

    private static Account current;

    private Session() {
    }

    public static synchronized Account current() {
        return current;
    }

    public static synchronized Account require() {
        if (current == null) {
            throw new IllegalStateException("Chưa đăng nhập");
        }
        return current;
    }

    public static synchronized void set(Account account) {
        current = account;
    }

    public static synchronized void clear() {
        current = null;
    }

    public static synchronized boolean isAdmin() {
        return current != null && current.isAdmin();
    }
}
