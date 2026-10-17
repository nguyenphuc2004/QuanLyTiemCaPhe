package com.tiemcaphe.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashVerifiesAndSaltsDiffer() {
        String h1 = PasswordHasher.hash("admin123");
        String h2 = PasswordHasher.hash("admin123");
        assertNotEquals(h1, h2, "mỗi lần băm phải có salt khác nhau");
        assertTrue(h1.startsWith("pbkdf2$"));
        assertTrue(PasswordHasher.verify("admin123", h1));
        assertTrue(PasswordHasher.verify("admin123", h2));
    }

    @Test
    void wrongOrMalformedInputFails() {
        String h = PasswordHasher.hash("secret");
        assertFalse(PasswordHasher.verify("Secret", h));
        assertFalse(PasswordHasher.verify("secret", "abc"));
        assertFalse(PasswordHasher.verify("secret", "pbkdf2$x$y$z"));
        assertFalse(PasswordHasher.verify(null, h));
        assertFalse(PasswordHasher.verify("secret", null));
    }
}
