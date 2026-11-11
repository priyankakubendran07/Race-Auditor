package com.raceauditor.ui.expenses;

import com.raceauditor.dao.ExpenseDAO;
import com.raceauditor.model.Expense;
import com.raceauditor.model.User;
import com.raceauditor.util.CurrencyUtil;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.UiTheme;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ExpensePanel extends JPanel {
    private final User user;
    private final ExpenseDAO dao = new ExpenseDAO();
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Date", "Description", "Category", "Amount", "Payment", "Notes"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = UiTheme.field(14);
    private final JComboBox<String> catFilter = new JComboBox<>(new String[]{"All","Food","Travel","Shopping","Education","Bills","Entertainment","Health","Other"});
    private final JLabel totalLabel = new JLabel();

    public ExpensePanel(User user) {
        this.user = user;
        setBackground(UiTheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel h = new JLabel("Expenses");
        h.setFont(UiTheme.TITLE);
        h.setForeground(Color.WHITE);
        totalLabel.setFont(UiTheme.HEADING);
        totalLabel.setForeground(UiTheme.GREEN);
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(UiTheme.BG);
        head.add(h, BorderLayout.WEST);
        head.add(totalLabel, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        JPanel toolbar = UiTheme.card();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        styleCombo(catFilter);
        toolbar.add(search);
        search.setPreferredSize(new Dimension(170, 32));
        toolbar.add(catFilter);
        JButton go = UiTheme.primaryButton("Search");
        go.setPreferredSize(new Dimension(120, 34));
        go.addActionListener(e -> refresh());
        JButton add = UiTheme.ghostButton("+ Add");
        add.setPreferredSize(new Dimension(110, 34));
        add.addActionListener(e -> openDialog(null));
        JButton edit = UiTheme.ghostButton("Edit");
        edit.setPreferredSize(new Dimension(90, 34));
        JButton del = UiTheme.ghostButton("Delete");
        del.setPreferredSize(new Dimension(90, 34));
        toolbar.add(go);
        toolbar.add(add);
        toolbar.add(edit);
        toolbar.add(del);
        for (Component c : toolbar.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 13));
            }
        }

        edit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { Dialogs.error(this, "Select an expense to edit."); return; }
            int id = (int) model.getValueAt(table.convertRowIndexToModel(r), 0);
            openDialog(loadById(id));
        });
        del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { Dialogs.error(this, "Select an expense to delete."); return; }
            int id = (int) model.getValueAt(table.convertRowIndexToModel(r), 0);
            if (!Dialogs.confirm(this, "Delete expense #" + id + "?")) return;
            try { dao.delete(id, user.getId()); refresh(); }
            catch (Exception ex) { Dialogs.error(this, "Delete failed: " + ex.getMessage()); }
        });

        UiTheme.styleTable(table);
        UiTheme.installReadableCellRenderer(table);
        table.setRowSorter(new TableRowSorter<>(model));
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(UiTheme.BG);
        center.add(toolbar, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(UiTheme.CARD);
        sp.getViewport().setBackground(UiTheme.CARD);
        sp.setBorder(BorderFactory.createEmptyBorder());
        center.add(sp, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        UiTheme.fixHierarchy(this);
    }

    private void styleCombo(JComboBox<String> c) {
        UiTheme.styleCombo(c);
        c.setPreferredSize(new Dimension(140, 32));
    }

    private Expense loadById(int id) {
        try {
            for (Expense e : dao.findByUser(user.getId(), null, "All", null, null))
                if (e.getId() == id) return e;
        } catch (Exception ignored) {}
        return null;
    }

    private void openDialog(Expense existing) {
        ExpenseDialog d = new ExpenseDialog((Frame) SwingUtilities.getWindowAncestor(this), user, existing);
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    public void refresh() {
        try {
            String q = search.getText();
            String c = (String) catFilter.getSelectedItem();
            List<Expense> list = dao.findByUser(user.getId(), q, c, null, null);
            model.setRowCount(0);
            double sum = 0;
            for (Expense e : list) {
                sum += e.getAmount();
                model.addRow(new Object[]{e.getId(), e.getDate().toString(), e.getTitle(), e.getCategory(),
                    String.format("%.2f", e.getAmount()), e.getPaymentMethod(), e.getNotes() == null ? "" : e.getNotes()});
            }
            totalLabel.setText("TOTAL  " + CurrencyUtil.inr(sum));
            UiTheme.fixHierarchy(this);
        } catch (Exception ex) {
            Dialogs.error(this, "Load failed: " + ex.getMessage());
        }
    }

    public void quickAdd() { openDialog(null); }
}
