package com.raceauditor.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** SHA-256 + random salt password hashing (no extra dependencies). */
public final class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String newSalt() {
        byte[] b = new byte[16];
        RANDOM.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest((salt + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            for (int i = 0; i < 999; i++) out = md.digest(out);
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new IllegalStateException("Hashing unavailable", e);
        }
    }

    public static boolean verify(String password, String salt, String expectedHash) {
        return hash(password, salt).equals(expectedHash);
    }
}
