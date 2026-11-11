package com.raceauditor.service;

import com.raceauditor.dao.CategoryDAO;
import com.raceauditor.model.Category;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Category business logic: per-category transaction counts and totals. */
public class CategoryService {
    private final CategoryDAO dao = new CategoryDAO();

    public List<Category> monthlySummary(int userId, int month, int year) throws Exception {
        return dao.summaryByUser(userId, month, year);
    }

    public Map<String, Double> categoryTotalsForCards(int userId, int month, int year) throws Exception {
        Map<String, Double> map = new LinkedHashMap<>();
        for (String c : ExpenseService.CATEGORIES) map.put(c, 0.0);
        for (Category s : monthlySummary(userId, month, year)) map.put(s.getCategory(), s.getTotal());
        return map;
    }
}
