package com.raceauditor.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Indian Rupee formatting. */
public final class CurrencyUtil {
    private static final NumberFormat INR =
        NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private CurrencyUtil() {}

    public static String inr(double amount) {
        return INR.format(amount);
    }
}
