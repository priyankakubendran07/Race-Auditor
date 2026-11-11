package com.raceauditor.ui.dashboard;

import com.raceauditor.dao.ExpenseDAO;
import com.raceauditor.model.Category;
import com.raceauditor.model.Expense;
import com.raceauditor.model.User;
import com.raceauditor.service.CategoryService;
import com.raceauditor.ui.MainFrame;
import com.raceauditor.util.CurrencyUtil;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.UiTheme;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Premium dark dashboard: headline cards, category bars, recent activity. */
public class DashboardPanel extends JPanel {
    private final User user;
    private final MainFrame frame;
    private final ExpenseDAO dao = new ExpenseDAO();
    private final CategoryService categories = new CategoryService();
    private final JPanel cardsRow = new JPanel(new GridLayout(1, 4, 12, 12));
    private final MiniBars bars = new MiniBars();
    private final DefaultTableModel recentModel = new DefaultTableModel(
        new String[]{"Date", "Description", "Category", "Amount"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JLabel helloLabel = new JLabel();

    public DashboardPanel(User user, MainFrame frame) {
        this.user = user;
        this.frame = frame;
        setBackground(UiTheme.BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(4, 4, 4, 4));

        helloLabel.setFont(UiTheme.LABEL);
        helloLabel.setForeground(UiTheme.BLUE);
        JLabel h = new JLabel("Expense Overview");
        h.setFont(UiTheme.TITLE);
        h.setForeground(Color.WHITE);

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(UiTheme.BG);
        JPanel titles = new JPanel();
        titles.setBackground(UiTheme.BG);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(helloLabel);
        titles.add(h);
        head.add(titles, BorderLayout.WEST);
        JButton cmpBtn = UiTheme.primaryButton("⇄  Compare Prices");
        cmpBtn.addActionListener(e -> frame.go("CMP"));
        head.add(cmpBtn, BorderLayout.EAST);
        add(head);
        add(Box.createVerticalStrut(14));

        cardsRow.setBackground(UiTheme.BG);
        add(cardsRow);
        add(Box.createVerticalStrut(12));

        JPanel mid = new JPanel(new GridLayout(1, 2, 12, 12));
        mid.setBackground(UiTheme.BG);

        JPanel recentCard = UiTheme.card();
        recentCard.setLayout(new BorderLayout(0, 8));
        JLabel rh = new JLabel("RECENT EXPENSES");
        rh.setFont(UiTheme.LABEL);
        rh.setForeground(UiTheme.MUTED);
        JTable t = new JTable(recentModel);
        UiTheme.styleTable(t);
        UiTheme.installReadableCellRenderer(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setBackground(UiTheme.CARD);
        sp.getViewport().setBackground(UiTheme.CARD);
        sp.setBorder(BorderFactory.createEmptyBorder());
        recentCard.add(rh, BorderLayout.NORTH);
        recentCard.add(sp, BorderLayout.CENTER);

        JPanel right = UiTheme.card();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel bh = new JLabel("TOP CATEGORIES");
        bh.setFont(UiTheme.LABEL);
        bh.setForeground(UiTheme.MUTED);
        right.add(bh);
        right.add(Box.createVerticalStrut(8));
        right.add(bars);
        right.add(Box.createVerticalStrut(12));
        JButton addBtn = UiTheme.ghostButton("+  Add Expense");
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.addActionListener(e -> frame.go("EXP"));
        right.add(addBtn);
        right.add(Box.createVerticalStrut(8));
        JLabel note = new JLabel("Demo prices — not live marketplace data.");
        note.setFont(UiTheme.SMALL);
        note.setForeground(UiTheme.MUTED);
        right.add(note);

        mid.add(recentCard);
        mid.add(right);
        add(mid);
        UiTheme.fixHierarchy(this);
    }

    public void refresh() {
        try {
            LocalDate now = LocalDate.now();
            helloLabel.setText(("WELCOME BACK, " + user.getFullName()).toUpperCase() + "  •  " + now.getMonth() + " " + now.getYear());
            double total = dao.monthlyTotal(user.getId(), now.getMonthValue(), now.getYear());
            List<Category> cats = categories.monthlySummary(user.getId(), now.getMonthValue(), now.getYear());
            long txns = cats.stream().mapToLong(Category::getCount).sum();
            String top = cats.isEmpty() ? "—" : cats.get(0).getCategory();

            cardsRow.removeAll();
            cardsRow.add(statCard("TOTAL EXPENSES", CurrencyUtil.inr(total), UiTheme.PURPLE));
            cardsRow.add(statCard("THIS MONTH", now.getMonth().toString().charAt(0) + now.getMonth().toString().substring(1).toLowerCase(), UiTheme.BLUE));
            cardsRow.add(statCard("TOP CATEGORY", top, UiTheme.CYAN));
            cardsRow.add(statCard("TRANSACTIONS", String.valueOf(txns), UiTheme.GREEN));
            cardsRow.revalidate();
            cardsRow.repaint();
            UiTheme.fixHierarchy(this);
            bars.setData(cats);

            recentModel.setRowCount(0);
            List<Expense> all = dao.findByUser(user.getId(), null, "All", now.getMonthValue(), now.getYear());
            for (int i = 0; i < Math.min(8, all.size()); i++) {
                Expense e = all.get(i);
                recentModel.addRow(new Object[]{e.getDate().toString(), e.getTitle(), e.getCategory(), CurrencyUtil.inr(e.getAmount())});
            }
        } catch (Exception ex) {
            Dialogs.error(this, "Dashboard load failed: " + ex.getMessage());
        }
    }

    private JPanel statCard(String title, String value, Color accent) {
        JPanel p = UiTheme.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel bar = new JLabel(" ");
        bar.setOpaque(true);
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(36, 4));
        bar.setMaximumSize(new Dimension(36, 4));
        JLabel t = new JLabel(title);
        t.setFont(UiTheme.LABEL);
        // Background-aware: dark slate text on white/light cards, muted on dark cards.
        t.setForeground(UiTheme.mutedFor(p.getBackground()));
        JLabel v = new JLabel(value);
        v.setFont(UiTheme.CARD_VALUE);
        // Background-aware: dark slate text on white/light cards, white on dark cards.
        v.setForeground(UiTheme.ensureFg(Color.WHITE, p.getBackground()));
        p.add(bar);
        p.add(Box.createVerticalStrut(8));
        p.add(t);
        p.add(v);
        return p;
    }

    /** Compact horizontal category bars, pure Swing painting. */
    static class MiniBars extends JPanel {
        private List<Category> data = List.of();
        MiniBars() { setBackground(UiTheme.CARD); setPreferredSize(new Dimension(300, 190)); }
        void setData(List<Category> d) { this.data = d.stream().limit(5).toList(); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (data.isEmpty()) {
                g2.setColor(UiTheme.MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString("No spending yet this month.", 12, 24);
                return;
            }
            double max = data.stream().mapToDouble(Category::getTotal).max().orElse(1);
            Color[] cols = {UiTheme.PURPLE, UiTheme.BLUE, UiTheme.CYAN, UiTheme.GREEN, UiTheme.GOLD};
            int y = 8;
            for (int i = 0; i < data.size(); i++) {
                Category s = data.get(i);
                g2.setColor(UiTheme.TEXT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(s.getCategory(), 12, y + 12);
                g2.setColor(UiTheme.MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String amt = CurrencyUtil.inr(s.getTotal());
                g2.drawString(amt, getWidth() - 12 - g2.getFontMetrics().stringWidth(amt), y + 12);
                int bw = (int) ((getWidth() - 24) * (s.getTotal() / max));
                g2.setColor(new Color(0x1E2740));
                g2.fillRoundRect(12, y + 18, getWidth() - 24, 8, 4, 4);
                g2.setColor(cols[i % cols.length]);
                g2.fillRoundRect(12, y + 18, Math.max(8, bw), 8, 4, 4);
                y += 36;
            }
        }
    }
}
