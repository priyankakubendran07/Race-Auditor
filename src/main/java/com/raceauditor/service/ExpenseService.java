package com.raceauditor.service;

import com.raceauditor.dao.ExpenseDAO;
import com.raceauditor.model.Expense;
import java.util.*;

public class ExpenseService {
    public static final List<String> CATEGORIES = List.of("Food","Travel","Shopping","Education","Bills","Entertainment","Health","Other");
    public static final List<String> PAYMENT_METHODS = List.of("Cash","UPI","Card","Net Banking","Wallet","Other");

    private final ExpenseDAO dao = new ExpenseDAO();

    public void save(Expense e) throws Exception {
        validate(e);
        if (e.getId() == 0) dao.insert(e);
        else dao.update(e);
    }

    private void validate(Expense e) {
        if (e.getTitle() == null || e.getTitle().isBlank()) throw new IllegalArgumentException("Expense name is required.");
        if (e.getDate() == null) throw new IllegalArgumentException("Date is required.");
        if (e.getAmount() <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        if (!CATEGORIES.contains(e.getCategory())) throw new IllegalArgumentException("Choose a valid category.");
    }
}
