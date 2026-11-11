package com.raceauditor.database;

import com.raceauditor.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MySQL connection manager (JDBC only, no embedded fallback per requirements).
 * On connection failure callers show a clear error dialog.
 */
public final class DatabaseManager {
    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.dbUrl(), AppConfig.dbUser(), AppConfig.dbPassword());
    }

    /** Creates tables if missing (safe to call on every startup). Does NOT seed demo products here. */
    public static void initSchema() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  full_name VARCHAR(120) NOT NULL,
                  email VARCHAR(160) NOT NULL UNIQUE,
                  username VARCHAR(60) NOT NULL UNIQUE,
                  password_hash VARCHAR(128) NOT NULL,
                  salt VARCHAR(64) NOT NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci""");
            s.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  user_id INT NOT NULL,
                  expense_date DATE NOT NULL,
                  title VARCHAR(160) NOT NULL,
                  category VARCHAR(30) NOT NULL,
                  amount DECIMAL(12,2) NOT NULL,
                  payment_method VARCHAR(40) NOT NULL DEFAULT 'Cash',
                  notes VARCHAR(500),
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                  CONSTRAINT chk_expenses_category CHECK (category IN ('Food','Travel','Shopping','Education','Bills','Entertainment','Health','Other')),
                  CONSTRAINT chk_expenses_amount CHECK (amount > 0),
                  KEY idx_expenses_user_date (user_id, expense_date),
                  KEY idx_expenses_user_cat (user_id, category)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci""");
            s.execute("""
                CREATE TABLE IF NOT EXISTS products (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(160) NOT NULL,
                  brand VARCHAR(100) NOT NULL DEFAULT '',
                  keywords TEXT NOT NULL,
                  KEY idx_products_name (name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci""");
            s.execute("""
                CREATE TABLE IF NOT EXISTS price_listings (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  product_id INT NOT NULL,
                  marketplace VARCHAR(40) NOT NULL,
                  price DECIMAL(12,2) NOT NULL,
                  availability VARCHAR(40) NOT NULL DEFAULT 'In Stock',
                  product_url VARCHAR(1024) NOT NULL,
                  is_demo TINYINT(1) NOT NULL DEFAULT 1,
                  CONSTRAINT fk_listings_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                  CONSTRAINT chk_listings_price CHECK (price > 0),
                  KEY idx_listings_product (product_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci""");
        }
    }

    public static String friendlyError(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        if (msg.contains("Communications link failure") || msg.contains("Connection refused")
                || msg.contains("Unknown host") || state.startsWith("08")) {
            return "Cannot connect to MySQL at " + AppConfig.dbUrl()
                + "\n\n1. Make sure MySQL is running on port 3307.\n"
                + "2. Create database 'expense_manager' and run database/schema.sql.\n"
                + "3. Check src/main/resources/config/db.properties (url/user/password).\n\nDetails: " + msg;
        }
        if (msg.contains("Access denied") || msg.contains("password authentication")) {
            return "MySQL rejected the username/password.\nCheck src/main/resources/config/db.properties.\n\nDetails: " + msg;
        }
        if (msg.contains("Unknown database")) {
            return "Database 'expense_manager' does not exist.\nRun database/schema.sql with MySQL.\n\nDetails: " + msg;
        }
        return "Database error: " + msg;
    }
}
