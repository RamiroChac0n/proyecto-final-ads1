package com.mycompany.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilsTest {

    @Test
    public void testHashPassword() {
        String password = "myPassword";
        String hashedPassword = PasswordUtils.hashPassword(password);

        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
    }

    @Test
    public void testCheckPassword() {
        String password = "myPassword";
        String hashedPassword = PasswordUtils.hashPassword(password);

        assertTrue(PasswordUtils.checkPassword(password, hashedPassword));
        assertFalse(PasswordUtils.checkPassword("wrongPassword", hashedPassword));
    }
}