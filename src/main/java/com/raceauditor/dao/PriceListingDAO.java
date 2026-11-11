package com.raceauditor.dao;

import com.raceauditor.database.DatabaseManager;
import com.raceauditor.model.PriceListing;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Marketplace price listings for a product (demo/sample data, always labelled as such in the UI). */
public class PriceListingDAO {

    public List<PriceListing> listingsFor(int productId) throws SQLException {
        String sql = "SELECT id, product_id, marketplace, price, availability, product_url, is_demo FROM price_listings WHERE product_id=? ORDER BY price ASC";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                List<PriceListing> out = new ArrayList<>();
                while (rs.next()) {
                    PriceListing l = new PriceListing();
                    l.setId(rs.getInt("id"));
                    l.setProductId(rs.getInt("product_id"));
                    l.setMarketplace(rs.getString("marketplace"));
                    l.setPrice(rs.getDouble("price"));
                    l.setAvailability(rs.getString("availability"));
                    l.setProductUrl(rs.getString("product_url"));
                    l.setDemo(rs.getBoolean("is_demo"));
                    out.add(l);
                }
                return out;
            }
        }
    }
}
