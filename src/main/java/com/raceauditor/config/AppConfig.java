package com.raceauditor.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Central configuration: loads db.properties from classpath, allows env overrides. */
public final class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("config/db.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException e) {
            System.err.println("Could not load db.properties: " + e.getMessage());
        }
    }

    private AppConfig() {}

    public static String dbUrl() {
        String env = System.getenv("RACEAUDITOR_DB_URL");
        if (env != null && !env.isBlank()) return env;
        return PROPS.getProperty("db.url",
            "jdbc:mysql://127.0.0.1:3307/expense_manager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
    }

    public static String dbUser() {
        String env = System.getenv("RACEAUDITOR_DB_USER");
        if (env != null && !env.isBlank()) return env;
        return PROPS.getProperty("db.user", "root");
    }

    public static String dbPassword() {
        String env = System.getenv("RACEAUDITOR_DB_PASSWORD");
        if (env != null && !env.isBlank()) return env;
        return PROPS.getProperty("db.password", "");
    }
}
