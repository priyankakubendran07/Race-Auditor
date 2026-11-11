package com.raceauditor.ui;

import com.raceauditor.model.User;
import com.raceauditor.ui.categories.CategoryPanel;
import com.raceauditor.ui.comparison.PriceComparisonPanel;
import com.raceauditor.ui.dashboard.DashboardPanel;
import com.raceauditor.ui.expenses.ExpensePanel;
import com.raceauditor.ui.login.LoginFrame;
import com.raceauditor.ui.profile.ProfilePanel;
import com.raceauditor.ui.reports.ReportsPanel;
import com.raceauditor.util.UiTheme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
    private final User user;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final List<JToggleButton> navButtons = new ArrayList<>();
    private DashboardPanel dashboard;
    private ExpensePanel expenses;
    private CategoryPanel categories;
    private PriceComparisonPanel comparison;
    private ReportsPanel reports;
    private ProfilePanel profile;

    public MainFrame(User user) {
        super("Race Auditor — " + user.getUsername());
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UiTheme.BG);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel side = new JPanel();
        side.setBackground(UiTheme.SIDEBAR);
        side.setPreferredSize(new Dimension(232, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UiTheme.BORDER));

        JLabel brand = new JLabel("  ◈ RACE AUDITOR");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brand.setForeground(Color.WHITE);
        brand.setBorder(new EmptyBorder(20, 10, 6, 10));
        side.add(brand);

        JLabel who = new JLabel("  " + user.getFullName());
        who.setFont(UiTheme.SMALL);
        who.setForeground(UiTheme.BLUE);
        who.setBorder(new EmptyBorder(0, 10, 16, 10));
        side.add(who);

        dashboard = new DashboardPanel(user, this);
        expenses = new ExpensePanel(user);
        categories = new CategoryPanel(user);
        comparison = new PriceComparisonPanel(user);
        reports = new ReportsPanel(user);
        profile = new ProfilePanel(user);
        content.add(dashboard, "DASH");
        content.add(expenses, "EXP");
        content.add(categories, "CAT");
        content.add(comparison, "CMP");
        content.add(reports, "REP");
        content.add(profile, "PRO");

        String[][] items = {
            {"◉", "Dashboard", "DASH"}, {"✎", "Expenses", "EXP"}, {"▦", "Categories", "CAT"},
            {"⇄", "Price Comparison", "CMP"}, {"▤", "Reports", "REP"}, {"◍", "Profile", "PRO"},
            {"⏻", "Logout", "OUT"}
        };
        ButtonGroup g = new ButtonGroup();
        for (int i = 0; i < items.length; i++) {
            JToggleButton b = UiTheme.navButton(items[i][0], items[i][1]);
            final String key = items[i][2];
            final int idx = i;
            b.addActionListener(e -> {
                if (key.equals("OUT")) {
                    dispose();
                    new LoginFrame(null).setVisible(true);
                    return;
                }
                select(idx);
                refresh(key);
                cards.show(content, key);
            });
            g.add(b);
            navButtons.add(b);
            side.add(b);
        }
        select(0);

        add(side, BorderLayout.WEST);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UiTheme.BG);
        wrap.setBorder(new EmptyBorder(18, 18, 18, 18));
        content.setBackground(UiTheme.BG);
        wrap.add(content, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
        refresh("DASH");
        UiTheme.fixHierarchy(getContentPane());
    }

    private void select(int idx) {
        for (int i = 0; i < navButtons.size(); i++) UiTheme.styleNav(navButtons.get(i), i == idx);
    }

    public void go(String key) {
        String[] keys = {"DASH", "EXP", "CAT", "CMP", "REP", "PRO"};
        for (int i = 0; i < keys.length; i++) if (keys[i].equals(key)) select(i);
        refresh(key);
        cards.show(content, key);
    }

    private void refresh(String key) {
        try {
            switch (key) {
                case "DASH" -> dashboard.refresh();
                case "EXP" -> expenses.refresh();
                case "CAT" -> categories.refresh();
                case "REP" -> reports.refresh();
                default -> {}
            }
        } catch (Exception ignored) {}
    }

    public PriceComparisonPanel getComparison() { return comparison; }
    public ExpensePanel getExpenses() { return expenses; }
}
