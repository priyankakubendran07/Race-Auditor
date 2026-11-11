package com.raceauditor.util;

import java.awt.Desktop;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Builds marketplace search URLs and opens the user's default browser. Never embeds a browser. */
public final class MarketplaceLinkBuilder {
    private MarketplaceLinkBuilder() {}

    public static String amazonSearch(String query) {
        return "https://www.amazon.in/s?k=" + enc(query);
    }

    public static String flipkartSearch(String query) {
        return "https://www.flipkart.com/search?q=" + enc(query);
    }

    public static String meeshoSearch(String query) {
        return "https://www.meesho.com/search?q=" + enc(query);
    }

    private static String enc(String s) {
        try { return URLEncoder.encode(s, StandardCharsets.UTF_8.toString()).replace("+", "+"); }
        catch (UnsupportedEncodingException e) { return s.replace(" ", "+"); }
    }

    public static void openInBrowser(String url) throws Exception {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            throw new IllegalStateException("Browser opening is not supported on this system. Copy the link manually:\n" + url);
        Desktop.getDesktop().browse(new URI(url));
    }
}
