package com.raceauditor.service;

import com.raceauditor.dao.CategoryDAO;
import com.raceauditor.dao.ExpenseDAO;
import com.raceauditor.model.Category;
import com.raceauditor.model.Expense;
import java.util.List;

/** Report business logic: monthly aggregates for the Reports screen. */
public class ReportService {
    public static class MonthlyReport {
        public final double total;
        public final int transactions;
        public final String topCategory;
        public final List<Category> byCategory;

        public MonthlyReport(double total, int transactions, String topCategory, List<Category> byCategory) {
            this.total = total;
            this.transactions = transactions;
            this.topCategory = topCategory;
            this.byCategory = byCategory;
        }
    }

    private final ExpenseDAO expenses = new ExpenseDAO();
    private final CategoryDAO categories = new CategoryDAO();

    public MonthlyReport monthly(int userId, int month, int year) throws Exception {
        double total = expenses.monthlyTotal(userId, month, year);
        List<Expense> list = expenses.findByUser(userId, null, "All", month, year);
        List<Category> cats = categories.summaryByUser(userId, month, year);
        String top = cats.isEmpty() ? "—" : cats.get(0).getCategory();
        return new MonthlyReport(total, list.size(), top, cats);
    }
}
