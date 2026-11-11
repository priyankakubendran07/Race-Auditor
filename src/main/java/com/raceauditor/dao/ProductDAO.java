package com.raceauditor.dao;

import com.raceauditor.database.DatabaseManager;
import com.raceauditor.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    /** Free-text search across name + keywords (supports any user-typed product). */
    public List<Product> search(String query) throws SQLException {
        String[] tokens = query.toLowerCase().trim().split("\\s+");
        StringBuilder sql = new StringBuilder("SELECT id, name, brand, keywords FROM products WHERE ");
        List<String> conds = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) conds.add("(LOWER(name) LIKE ? OR LOWER(keywords) LIKE ? OR LOWER(brand) LIKE ?)");
        sql.append(String.join(" OR ", conds)).append(" ORDER BY name LIMIT 12");
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int p = 1;
            for (String t : tokens) {
                String like = "%" + t + "%";
                ps.setString(p++, like);
                ps.setString(p++, like);
                ps.setString(p++, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> out = new ArrayList<>();
                while (rs.next()) {
                    Product pr = new Product();
                    pr.setId(rs.getInt("id"));
                    pr.setName(rs.getString("name"));
                    pr.setBrand(rs.getString("brand"));
                    pr.setKeywords(rs.getString("keywords"));
                    out.add(pr);
                }
                return out;
            }
        }
    }

    public boolean isEmpty() throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM products")) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }
}
