package com.raceauditor.util;

/** Simple field validation helpers. */
public final class Validator {
    private Validator() {}

    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static boolean isEmail(String s) {
        return s != null && s.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
