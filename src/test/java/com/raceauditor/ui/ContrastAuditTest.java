package com.raceauditor.ui;

import com.raceauditor.model.User;
import com.raceauditor.ui.categories.CategoryPanel;
import com.raceauditor.ui.comparison.PriceComparisonPanel;
import com.raceauditor.ui.dashboard.DashboardPanel;
import com.raceauditor.ui.expenses.ExpensePanel;
import com.raceauditor.ui.login.LoginFrame;
import com.raceauditor.ui.login.RegisterDialog;
import com.raceauditor.ui.profile.ProfilePanel;
import com.raceauditor.ui.reports.ReportsPanel;
import com.raceauditor.util.UiTheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Readability guard: every screen must satisfy
 * dark background -&gt; light text, light background -&gt; dark text,
 * with a minimum contrast ratio and readable font sizes.
 */
class ContrastAuditTest {

    private static final double MIN_CONTRAST = 3.0;
    private static final int MIN_FONT = 12;

    @BeforeAll
    static void setUp() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "No display available");
        UiTheme.apply();
    }

    @AfterAll
    static void tearDown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (Window w : Window.getWindows()) w.dispose();
        });
    }

    @Test
    void everyScreenPassesContrastAndSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            User user = new User();
            List<String> failures = new ArrayList<>();
            audit("LoginFrame", new LoginFrame(null), failures);
            audit("SplashFrame", new com.raceauditor.ui.login.SplashFrame(), failures);
            audit("DashboardPanel", wrap(new DashboardPanel(user, null)), failures);
            audit("ExpensePanel", wrap(new ExpensePanel(user)), failures);
            audit("CategoryPanel", wrap(new CategoryPanel(user)), failures);
            audit("PriceComparisonPanel", wrap(new PriceComparisonPanel(user)), failures);
            audit("ReportsPanel", wrap(new ReportsPanel(user)), failures);
            audit("ProfilePanel", wrap(new ProfilePanel(user)), failures);
            for (Window w : Window.getWindows()) w.dispose();
            assertTrue(failures.isEmpty(),
                "Contrast/size failures:\n" + String.join("\n", failures));
        });
    }

    private static JFrame wrap(JPanel panel) {
        JFrame f = new JFrame();
        f.setContentPane(panel);
        f.pack();
        return f;
    }

    private static void audit(String screen, Window window, List<String> failures) {
        for (Component c : allDescendants(window)) {
            if (isLafChrome(c)) continue; // native scrollbar/combo arrows paint icons, not text
            if (c instanceof JLabel l) check(screen, "JLabel", "'" + trim(l.getText()) + "'",
                l.getForeground(), bgOf(l), trim(l.getText()).isEmpty() ? null : l.getFont(), failures);
            else if (c instanceof AbstractButton b) check(screen, b.getClass().getSimpleName(), "'" + trim(b.getText()) + "'",
                b.getForeground(), bgOf(b), b.getFont(), failures);
            else if (c instanceof JTextComponent t) check(screen, "TextField", "'input'",
                t.getForeground(), bgOf(t), t.getFont(), failures);
            else if (c instanceof JComboBox<?> cb) check(screen, "JComboBox", "'filter'",
                cb.getForeground(), bgOf(cb), cb.getFont(), failures);
            else if (c instanceof JTable t) {
                check(screen, "JTable", "'rows'", t.getForeground(), bgOf(t), t.getFont(), failures);
                JTableHeader h = t.getTableHeader();
                if (h != null) check(screen, "JTableHeader", "'headers'",
                    h.getForeground(), h.getBackground(), h.getFont(), failures);
            }
        }
    }

    private static void check(String screen, String kind, String what, Color fg, Color bg,
                              Font font, List<String> failures) {
        if (fg == null || bg == null) {
            failures.add(screen + " " + kind + " " + what + ": missing color");
            return;
        }
        double ratio = contrast(fg, bg);
        if (ratio < MIN_CONTRAST) {
            failures.add(String.format("%s %s %s: contrast %.2f (fg=%s bg=%s)",
                screen, kind, what, ratio, hex(fg), hex(bg)));
        }
        if (font != null && font.getSize() < MIN_FONT) {
            failures.add(String.format("%s %s %s: font size %d too small",
                screen, kind, what, font.getSize()));
        }
    }

    /** LAF-internal chrome (scrollbar arrows, combo arrow buttons): native icons, not text. */
    private static boolean isLafChrome(Component c) {
        String name = c.getClass().getSimpleName();
        if (name.contains("ArrowButton") || name.contains("ComboBoxButton")) return true;
        Component cur = c.getParent();
        while (cur != null) {
            if (cur instanceof javax.swing.JScrollBar) return true;
            cur = cur.getParent();
        }
        return false;
    }

    private static List<Component> allDescendants(Container root) {
        List<Component> out = new ArrayList<>();
        for (Component c : root.getComponents()) {
            out.add(c);
            if (c instanceof Container cont) out.addAll(allDescendants(cont));
        }
        return out;
    }

    /** Effective background: own painted background, else nearest painting ancestor. */
    private static Color bgOf(Component c) {
        Component cur = c;
        while (cur != null) {
            if (cur instanceof JTextComponent || cur instanceof JComboBox || cur instanceof JTable
                    || cur instanceof JTableHeader || cur instanceof JPanel || cur instanceof JViewport
                    || cur instanceof JScrollPane || cur instanceof JDialog || cur instanceof JFrame
                    || cur instanceof javax.swing.JWindow) {
                if (cur.getBackground() != null) return cur.getBackground();
            }
            cur = cur.getParent();
        }
        return Color.BLACK;
    }

    private static double contrast(Color a, Color b) {
        double l1 = luminance(a);
        double l2 = luminance(b);
        double hi = Math.max(l1, l2);
        double lo = Math.min(l1, l2);
        return (hi + 0.05) / (lo + 0.05);
    }

    private static double luminance(Color c) {
        double r = linear(c.getRed() / 255.0);
        double g = linear(c.getGreen() / 255.0);
        double bl = linear(c.getBlue() / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * bl;
    }

    private static double linear(double v) {
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    private static String hex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static String trim(String s) {
        if (s == null) return "";
        String t = s.replaceAll("<[^>]*>", "").trim();
        return t.length() > 40 ? t.substring(0, 40) + "..." : t;
    }
}
