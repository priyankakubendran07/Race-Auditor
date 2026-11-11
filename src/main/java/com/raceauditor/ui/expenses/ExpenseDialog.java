package com.raceauditor.ui.expenses;

import com.raceauditor.model.Expense;
import com.raceauditor.model.User;
import com.raceauditor.service.ExpenseService;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.UiTheme;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

public class ExpenseDialog extends JDialog {
    private boolean saved = false;
    private final Expense expense;
    private final ExpenseService service = new ExpenseService();
    private final JTextField date = UiTheme.field(12);
    private final JTextField title = UiTheme.field(20);
    private final JComboBox<String> category = new JComboBox<>(ExpenseService.CATEGORIES.toArray(new String[0]));
    private final JTextField amount = UiTheme.field(12);
    private final JComboBox<String> payment = new JComboBox<>(ExpenseService.PAYMENT_METHODS.toArray(new String[0]));
    private final JTextField notes = UiTheme.field(20);

    public ExpenseDialog(Frame owner, User user, Expense existing) {
        super(owner, existing == null ? "Add Expense" : "Edit Expense", true);
        this.expense = existing == null ? new Expense() : existing;
        if (existing == null) this.expense.setUserId(user.getId());
        setSize(400, 560);
        setMinimumSize(new Dimension(380, 520));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UiTheme.BG);

        JPanel p = new JPanel();
        p.setBackground(UiTheme.BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        styleCombo(category);
        styleCombo(payment);
        p.add(UiTheme.label("Date (YYYY-MM-DD)"));
        date.setText(this.expense.getDate() == null ? LocalDate.now().toString() : this.expense.getDate().toString());
        p.add(date);
        p.add(Box.createVerticalStrut(10));
        p.add(UiTheme.label("Expense Name"));
        title.setText(nvl(this.expense.getTitle()));
        p.add(title);
        p.add(Box.createVerticalStrut(10));
        p.add(UiTheme.label("Category"));
        if (this.expense.getCategory() != null) category.setSelectedItem(this.expense.getCategory());
        p.add(category);
        p.add(Box.createVerticalStrut(10));
        p.add(UiTheme.label("Amount (₹)"));
        amount.setText(this.expense.getAmount() == 0 ? "" : String.valueOf(this.expense.getAmount()));
        p.add(amount);
        p.add(Box.createVerticalStrut(10));
        p.add(UiTheme.label("Payment Method"));
        if (this.expense.getPaymentMethod() != null) payment.setSelectedItem(this.expense.getPaymentMethod());
        p.add(payment);
        p.add(Box.createVerticalStrut(10));
        p.add(UiTheme.label("Notes"));
        notes.setText(nvl(this.expense.getNotes()));
        p.add(notes);
        p.add(Box.createVerticalStrut(16));
        JButton save = UiTheme.primaryButton("Save Expense");
        save.addActionListener(e -> doSave());
        p.add(save);
        add(new JScrollPane(p));
        UiTheme.fixHierarchy(p);
    }

    private void styleCombo(JComboBox<String> c) {
        UiTheme.styleCombo(c);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private void doSave() {
        try {
            expense.setDate(LocalDate.parse(date.getText().trim()));
            expense.setTitle(title.getText().trim());
            expense.setCategory((String) category.getSelectedItem());
            expense.setAmount(Double.parseDouble(amount.getText().trim()));
            expense.setPaymentMethod((String) payment.getSelectedItem());
            expense.setNotes(notes.getText().trim());
            service.save(expense);
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            Dialogs.error(this, "Amount must be a valid number (e.g. 499.00).");
        } catch (java.time.format.DateTimeParseException ex) {
            Dialogs.error(this, "Date must be YYYY-MM-DD (e.g. 2026-10-04).");
        } catch (IllegalArgumentException ex) {
            Dialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            Dialogs.error(this, "Save failed: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}
