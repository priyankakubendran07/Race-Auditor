package com.raceauditor.ui.categories;

import com.raceauditor.dao.CategoryDAO;
import com.raceauditor.model.Category;
import com.raceauditor.model.User;
import com.raceauditor.service.ExpenseService;
import com.raceauditor.util.CurrencyUtil;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.UiTheme;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CategoryPanel extends JPanel {
    private final User user;
    private final CategoryDAO dao = new CategoryDAO();
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));

    public CategoryPanel(User user) {
        this.user = user;
        setBackground(UiTheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel h = new JLabel("Categories");
        h.setFont(UiTheme.TITLE);
        h.setForeground(Color.WHITE);
        JLabel s = new JLabel("Spending by category — this month");
        s.setFont(UiTheme.SUBTITLE);
        s.setForeground(UiTheme.MUTED);
        JPanel head = new JPanel();
        head.setBackground(UiTheme.BG);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(h);
        head.add(s);
        add(head, BorderLayout.NORTH);
        grid.setBackground(UiTheme.BG);
        JScrollPane sp = new JScrollPane(grid);
        sp.setBackground(UiTheme.BG);
        sp.getViewport().setBackground(UiTheme.BG);
        sp.setBorder(BorderFactory.createEmptyBorder());
        add(sp, BorderLayout.CENTER);
        UiTheme.fixHierarchy(this);
    }

    public void refresh() {
        try {
            LocalDate now = LocalDate.now();
            java.util.List<Category> rows = dao.summaryByUser(user.getId(), now.getMonthValue(), now.getYear());
            double max = rows.stream().mapToDouble(Category::getTotal).max().orElse(1);
            Map<String, Category> byCat = new HashMap<>();
            for (Category c : rows) byCat.put(c.getCategory(), c);
            grid.removeAll();
            Color[] cols = {UiTheme.PURPLE, UiTheme.BLUE, UiTheme.CYAN, UiTheme.GREEN, UiTheme.GOLD, UiTheme.MEESHO, UiTheme.AMAZON, UiTheme.FLIPKART};
            int i = 0;
            for (String c : ExpenseService.CATEGORIES) {
                Category s = byCat.getOrDefault(c, new Category(c, 0, 0));
                grid.add(card(c, s.getCount(), s.getTotal(), s.getTotal() / max, cols[i++ % cols.length]));
            }
            grid.revalidate();
            grid.repaint();
            UiTheme.fixHierarchy(this);
        } catch (Exception ex) {
            Dialogs.error(this, "Load failed: " + ex.getMessage());
        }
    }

    private JPanel card(String name, long count, double total, double frac, Color accent) {
        JPanel p = UiTheme.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel n = new JLabel(name.toUpperCase());
        n.setFont(UiTheme.LABEL);
        n.setForeground(accent);
        JLabel t = new JLabel(CurrencyUtil.inr(total));
        t.setFont(UiTheme.CARD_VALUE);
        t.setForeground(Color.WHITE);
        JLabel c = new JLabel(count + " transactions");
        c.setFont(UiTheme.SMALL);
        c.setForeground(UiTheme.MUTED);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.round(frac * 100));
        bar.setStringPainted(false);
        bar.setForeground(accent);
        bar.setBackground(new Color(0x1E2740));
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        p.add(n);
        p.add(Box.createVerticalStrut(4));
        p.add(t);
        p.add(c);
        p.add(Box.createVerticalStrut(8));
        p.add(bar);
        return p;
    }
}
