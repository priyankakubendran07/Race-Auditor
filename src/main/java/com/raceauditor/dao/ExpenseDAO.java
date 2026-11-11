package com.raceauditor.dao;

import com.raceauditor.database.DatabaseManager;
import com.raceauditor.model.Expense;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public List<Expense> findByUser(int userId, String search, String category, Integer month, Integer year) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT id, user_id, expense_date, title, category, amount, payment_method, notes FROM expenses WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (search != null && !search.isBlank()) {
            sql.append(" AND (title LIKE ? OR notes LIKE ?)");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
        }
        if (category != null && !category.equals("All")) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (month != null && year != null) {
            sql.append(" AND MONTH(expense_date) = ? AND YEAR(expense_date) = ?");
            params.add(month);
            params.add(year);
        }
        sql.append(" ORDER BY expense_date DESC, id DESC");
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Expense> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    public void insert(Expense e) throws SQLException {
        String sql = "INSERT INTO expenses(user_id, expense_date, title, category, amount, payment_method, notes) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, e.getUserId());
            ps.setDate(2, Date.valueOf(e.getDate()));
            ps.setString(3, e.getTitle());
            ps.setString(4, e.getCategory());
            ps.setDouble(5, e.getAmount());
            ps.setString(6, e.getPaymentMethod());
            ps.setString(7, e.getNotes());
            ps.executeUpdate();
        }
    }

    public void update(Expense e) throws SQLException {
        String sql = "UPDATE expenses SET expense_date=?, title=?, category=?, amount=?, payment_method=?, notes=? WHERE id=? AND user_id=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(e.getDate()));
            ps.setString(2, e.getTitle());
            ps.setString(3, e.getCategory());
            ps.setDouble(4, e.getAmount());
            ps.setString(5, e.getPaymentMethod());
            ps.setString(6, e.getNotes());
            ps.setInt(7, e.getId());
            ps.setInt(8, e.getUserId());
            ps.executeUpdate();
        }
    }

    public void delete(int id, int userId) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM expenses WHERE id=? AND user_id=?")) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public double monthlyTotal(int userId, int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE user_id=? AND MONTH(expense_date)=? AND YEAR(expense_date)=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getDouble(1); }
        }
    }

    private Expense map(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getInt("id"));
        e.setUserId(rs.getInt("user_id"));
        e.setDate(LocalDate.parse(rs.getDate("expense_date").toString()));
        e.setTitle(rs.getString("title"));
        e.setCategory(rs.getString("category"));
        e.setAmount(rs.getDouble("amount"));
        e.setPaymentMethod(rs.getString("payment_method"));
        e.setNotes(rs.getString("notes"));
        return e;
    }
}
