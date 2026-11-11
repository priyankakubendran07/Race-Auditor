package com.raceauditor.ui.reports;

import com.raceauditor.dao.ExpenseDAO;
import com.raceauditor.model.Category;
import com.raceauditor.model.Expense;
import com.raceauditor.model.User;
import com.raceauditor.service.ReportService;
import com.raceauditor.service.ReportService.MonthlyReport;
import com.raceauditor.util.CurrencyUtil;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.ReportExporter;
import com.raceauditor.util.UiTheme;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ReportsPanel extends JPanel {
    private final User user;
    private final ReportService reports = new ReportService();
    private final JComboBox<String> monthBox = new JComboBox<>();
    private final JComboBox<Integer> yearBox = new JComboBox<>(new Integer[]{2024, 2025, 2026, 2027});
    private final JLabel totalLbl = new JLabel();
    private final JLabel countLbl = new JLabel();
    private final JLabel topLbl = new JLabel();
    private final BarPanel bar = new BarPanel();
    private final JButton go = UiTheme.primaryButton("Generate");
    private final JButton pdfBtn = UiTheme.ghostButton("Export PDF");
    private final JButton csvBtn = UiTheme.ghostButton("Export CSV");
    private final ExpenseDAO expenseDao = new ExpenseDAO();
    private final JLabel statusLbl = new JLabel();
    private volatile SwingWorker<MonthlyReport, Void> currentWorker;

    public ReportsPanel(User user) {
        this.user = user;
        setBackground(UiTheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel h = new JLabel("Reports");
        h.setFont(UiTheme.TITLE);
        h.setForeground(Color.WHITE);
        add(h, BorderLayout.NORTH);

        JPanel ctrl = UiTheme.card();
        ctrl.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        for (String m : months) monthBox.addItem(m);
        monthBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        yearBox.setSelectedItem(LocalDate.now().getYear());
        styleCombo(monthBox);
        styleCombo(yearBox);
        JButton go = this.go;
        go.setPreferredSize(new Dimension(130, 34));
        go.addActionListener(e -> refreshAsync());
        pdfBtn.setPreferredSize(new Dimension(130, 34));
        csvBtn.setPreferredSize(new Dimension(130, 34));
        pdfBtn.addActionListener(e -> exportFile(true));
        csvBtn.addActionListener(e -> exportFile(false));
        statusLbl.setFont(UiTheme.SMALL);
        statusLbl.setForeground(UiTheme.MUTED);
        ctrl.add(new JLabel("Month:"));
        ctrl.add(monthBox);
        ctrl.add(new JLabel("Year:"));
        ctrl.add(yearBox);
        ctrl.add(go);
        ctrl.add(pdfBtn);
        ctrl.add(csvBtn);
        ctrl.add(statusLbl);
        for (Component c : ctrl.getComponents()) if (c instanceof JLabel) { c.setForeground(UiTheme.TEXT); c.setFont(UiTheme.BODY); }

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 12));
        stats.setBackground(UiTheme.BG);
        stats.add(statCard("MONTHLY TOTAL", totalLbl, UiTheme.PURPLE));
        stats.add(statCard("TRANSACTIONS", countLbl, UiTheme.BLUE));
        stats.add(statCard("TOP CATEGORY", topLbl, UiTheme.CYAN));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(UiTheme.BG);
        center.add(ctrl, BorderLayout.NORTH);
        center.add(stats, BorderLayout.CENTER);
        JPanel chartCard = UiTheme.card();
        chartCard.setLayout(new BorderLayout());
        JLabel ch = new JLabel("CATEGORY BREAKDOWN");
        ch.setFont(UiTheme.LABEL);
        ch.setForeground(UiTheme.MUTED);
        chartCard.add(ch, BorderLayout.NORTH);
        chartCard.add(bar, BorderLayout.CENTER);
        center.add(chartCard, BorderLayout.SOUTH);
        JScrollPane sp = new JScrollPane(center);
        sp.setBackground(UiTheme.BG);
        sp.getViewport().setBackground(UiTheme.BG);
        sp.setBorder(BorderFactory.createEmptyBorder());
        add(sp, BorderLayout.CENTER);
        UiTheme.fixHierarchy(this);
    }

    private void styleCombo(JComboBox<?> c) {
        UiTheme.styleCombo(c);
        c.setPreferredSize(new Dimension(130, 32));
    }

    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel p = UiTheme.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel bar = new JLabel(" ");
        bar.setOpaque(true);
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(36, 4));
        bar.setMaximumSize(new Dimension(36, 4));
        JLabel t = new JLabel(title);
        t.setFont(UiTheme.LABEL);
        t.setForeground(UiTheme.mutedFor(p.getBackground()));
        value.setFont(UiTheme.CARD_VALUE);
        value.setForeground(UiTheme.ensureFg(java.awt.Color.WHITE, p.getBackground()));
        p.add(bar);
        p.add(Box.createVerticalStrut(8));
        p.add(t);
        p.add(value);
        return p;
    }

    /** Exposed for automated UI verification. */
    JButton getGenerateButton() { return go; }

    /** Exposed for automated UI verification. */
    JButton getExportPdfButton() { return pdfBtn; }

    /** Exposed for automated UI verification. */
    JButton getExportCsvButton() { return csvBtn; }

    /**
     * Exports the currently selected month's real report data to PDF or CSV.
     * Generate logic is untouched; this only reads fresh data and writes a file.
     */
    private void exportFile(boolean pdf) {
        try {
            int month = monthBox.getSelectedIndex() + 1;
            Object selYear = yearBox.getSelectedItem();
            int year = selYear instanceof Integer i ? i : LocalDate.now().getYear();
            String monthName = (String) monthBox.getSelectedItem();
            MonthlyReport r = reports.monthly(user.getId(), month, year);
            List<Expense> items = expenseDao.findByUser(user.getId(), null, "All", month, year);
            String ext = pdf ? "pdf" : "csv";
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle(pdf ? "Export Report as PDF" : "Export Report as CSV");
            fc.setSelectedFile(new File("RaceAuditor-Report-" + monthName + "-" + year + "." + ext));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase(java.util.Locale.ROOT).endsWith("." + ext)) {
                f = new File(f.getParentFile(), f.getName() + "." + ext);
            }
            if (pdf) ReportExporter.exportPdf(f, monthName, year, r, items);
            else ReportExporter.exportCsv(f, monthName, year, r, items);
            statusLbl.setText("Saved " + f.getName());
            Dialogs.info(this, (pdf ? "PDF" : "CSV") + " exported with " + items.size()
                + " expense(s):\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            statusLbl.setText("Export failed.");
            Dialogs.error(this, "Export failed: " + ex.getMessage());
        } finally {
            UiTheme.fixHierarchy(this);
        }
    }

    /** Exposed for automated UI verification; waits for a pending Generate. */
    String getTotalText() {
        SwingWorker<MonthlyReport, Void> w = currentWorker;
        if (w != null && !w.isDone()) {
            try {
                MonthlyReport r = w.get(10, TimeUnit.SECONDS);
                applyToUi(r);
            } catch (TimeoutException | CancellationException ignored) {
            } catch (ExecutionException ex) {
                return totalLbl.getText();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        return totalLbl.getText();
    }

    /** Synchronous refresh (initial load, programmatic callers). */
    public void refresh() {
        try {
            int month = monthBox.getSelectedIndex() + 1;
            int year = (Integer) yearBox.getSelectedItem();
            MonthlyReport r = reports.monthly(user.getId(), month, year);
            applyToUi(r);
        } catch (Exception ex) {
            Dialogs.error(this, "Report failed: " + ex.getMessage());
        }
    }

    /**
     * Real-time Generate: reads the latest month/year on the EDT at click
     * time, fetches fresh data off the EDT in a SwingWorker, then updates
     * the UI back on the EDT with progress indication and error popup.
     */
    public void refreshAsync() {
        final int month = monthBox.getSelectedIndex() + 1;
        final Object selYear = yearBox.getSelectedItem();
        final int year = selYear instanceof Integer i ? i : LocalDate.now().getYear();
        SwingWorker<MonthlyReport, Void> prev = currentWorker;
        if (prev != null && !prev.isDone()) prev.cancel(true);
        go.setEnabled(false);
        statusLbl.setText("Generating...");
        currentWorker = new SwingWorker<>() {
            @Override
            protected MonthlyReport doInBackground() throws Exception {
                // Fresh database read at the moment of clicking; never cached.
                return reports.monthly(user.getId(), month, year);
            }

            @Override
            protected void done() {
                // Runs on the Event Dispatch Thread.
                try {
                    MonthlyReport r = get();
                    statusLbl.setText("");
                    applyToUi(r);
                } catch (CancellationException ignored) {
                    statusLbl.setText("");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    statusLbl.setText("Generate failed.");
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "Report was interrupted.", "Reports", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ex) {
                    statusLbl.setText("Generate failed.");
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "Report failed: " + cause.getMessage(), "Reports", JOptionPane.ERROR_MESSAGE);
                } finally {
                    go.setEnabled(true);
                    ReportsPanel.this.revalidate();
                    ReportsPanel.this.repaint();
                }
            }
        };
        currentWorker.execute();
    }

    /** Applies a loaded report to the UI (call on the EDT). */
    private void applyToUi(MonthlyReport r) {
        totalLbl.setText(CurrencyUtil.inr(r.total));
        countLbl.setText(String.valueOf(r.transactions));
        topLbl.setText(r.topCategory);
        bar.setData(r.byCategory);
        bar.revalidate();
        bar.repaint();
        ReportsPanel.this.revalidate();
        ReportsPanel.this.repaint();
        UiTheme.fixHierarchy(this);
    }

    /** Dark neon bar chart, pure Swing painting. */
    static class BarPanel extends JPanel {
        private List<Category> data = List.of();
        BarPanel() { setPreferredSize(new Dimension(600, 240)); setBackground(UiTheme.CARD); }
        void setData(List<Category> d) { this.data = d; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (data.isEmpty()) {
                g2.setColor(UiTheme.MUTED);
                g2.drawString("No data for selected month.", 20, 30);
                return;
            }
            double max = data.stream().mapToDouble(Category::getTotal).max().orElse(1);
            Color[] cols = {UiTheme.PURPLE, UiTheme.BLUE, UiTheme.CYAN, UiTheme.GREEN, UiTheme.GOLD, UiTheme.MEESHO, UiTheme.AMAZON, UiTheme.FLIPKART};
            int x = 20, i = 0;
            for (Category s : data) {
                int h = (int) (170 * (s.getTotal() / max));
                g2.setColor(cols[i++ % cols.length]);
                g2.fillRoundRect(x, 210 - h, 64, h, 8, 8);
                g2.setColor(UiTheme.MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String n = s.getCategory();
                g2.drawString(n.substring(0, Math.min(8, n.length())), x, 226);
                x += 82;
                if (x > getWidth() - 70) break;
            }
        }
    }
}
