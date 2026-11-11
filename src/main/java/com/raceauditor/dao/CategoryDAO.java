package com.raceauditor.dao;

import com.raceauditor.database.DatabaseManager;
import com.raceauditor.model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Category aggregates computed from the expenses table. */
public class CategoryDAO {

    public List<Category> summaryByUser(int userId, Integer month, Integer year) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT category, COUNT(*), COALESCE(SUM(amount),0) FROM expenses WHERE user_id=?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (month != null && year != null) {
            sql.append(" AND MONTH(expense_date)=? AND YEAR(expense_date)=?");
            params.add(month);
            params.add(year);
        }
        sql.append(" GROUP BY category ORDER BY SUM(amount) DESC");
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Category> out = new ArrayList<>();
                while (rs.next()) out.add(new Category(rs.getString(1), rs.getLong(2), rs.getDouble(3)));
                return out;
            }
        }
    }
}
