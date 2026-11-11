package com.raceauditor.util;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Dark neon design system: deep navy background, purple + sky-blue accents. */
public final class UiTheme {
    public static final Color BG = new Color(0x0B1020);
    public static final Color BG_DEEP = new Color(0x070B16);
    public static final Color CARD = new Color(0x141B2E);
    public static final Color CARD_2 = new Color(0x1A2338);
    public static final Color BORDER = new Color(0x2A3350);
    public static final Color TEXT = new Color(0xE8ECF4);
    public static final Color MUTED = new Color(0x9AA5BB);
    public static final Color PURPLE = new Color(0x8B5CF6);
    public static final Color PURPLE_DEEP = new Color(0x6D28D9);
    public static final Color BLUE = new Color(0x38BDF8);
    public static final Color CYAN = new Color(0x22D3EE);
    public static final Color GREEN = new Color(0x34D399);
    public static final Color GOLD = new Color(0xFBBF24);
    public static final Color RED = new Color(0xF87171);
    public static final Color SIDEBAR = new Color(0x0D1326);
    public static final Color NAV_ACTIVE = new Color(0x2A2350);
    public static final Color AMAZON = new Color(0xFFB84D);
    public static final Color FLIPKART = new Color(0x38BDF8);
    public static final Color MEESHO = new Color(0xF472B6);

    // ---- Global contrast rule: text must contrast with its actual background ----
    // Light background -> dark navy text. Dark background -> white/light text.
    public static final Color INK = new Color(0x0F1E3A);
    public static final Color INK_MUTED = new Color(0x334155);
    /** Dark slate text for white/light panels (e.g. dashboard/expense overview white boxes). */
    public static final Color LIGHT_PANEL_TEXT = new Color(31, 41, 55);
    /** Muted text for white/light panels. */
    public static final Color LIGHT_PANEL_MUTED = new Color(75, 85, 99);
    public static final Color PLACEHOLDER_ON_DARK = new Color(0x7C8AA5);
    public static final Color PLACEHOLDER_ON_LIGHT = new Color(0x64748B);
    /** Minimum acceptable WCAG-style contrast ratio for normal text. */
    public static final double MIN_CONTRAST = 3.0;

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font CARD_VALUE = new Font("Segoe UI", Font.BOLD, 22);

    private UiTheme() {}

    public static void apply() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        UIManager.put("Panel.background", CARD);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.disabledForeground", MUTED);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.background", CARD);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", NAV_ACTIVE);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", new Color(0x1E2740));
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TextField.background", CARD_2);
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.caretForeground", BLUE);
        UIManager.put("TextField.selectionBackground", NAV_ACTIVE);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextField.inactiveForeground", MUTED);
        UIManager.put("PasswordField.background", CARD_2);
        UIManager.put("PasswordField.foreground", Color.WHITE);
        UIManager.put("PasswordField.caretForeground", BLUE);
        UIManager.put("PasswordField.selectionBackground", NAV_ACTIVE);
        UIManager.put("PasswordField.selectionForeground", Color.WHITE);
        UIManager.put("PasswordField.inactiveForeground", MUTED);
        UIManager.put("ComboBox.background", CARD_2);
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", NAV_ACTIVE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.disabledForeground", MUTED);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.disabledText", MUTED);
        UIManager.put("CheckBox.background", BG);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("RadioButton.background", BG);
        UIManager.put("RadioButton.foreground", TEXT);
        UIManager.put("ToolTip.background", Color.WHITE);
        UIManager.put("ToolTip.foreground", INK);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBar.background", CARD_2);
        UIManager.put("ScrollBar.track", BG_DEEP);
        UIManager.put("ScrollBar.thumb", BORDER);
        UIManager.put("ScrollBar.foreground", TEXT);
        UIManager.put("ScrollBar.width", 14);
    }

    // ------------------------------------------------------------------
    // Central contrast utilities. Every panel must route text colors
    // through these so white/light backgrounds get dark text and
    // dark backgrounds get light text — never white-on-white or
    // dark-on-dark.
    // ------------------------------------------------------------------

    /** Relative luminance (0=black, 1=white) per WCAG. */
    public static double luminance(Color c) {
        double r = lin(c.getRed() / 255.0);
        double g = lin(c.getGreen() / 255.0);
        double b = lin(c.getBlue() / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double lin(double v) {
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    /** WCAG contrast ratio between two colors. */
    public static double contrastRatio(Color fg, Color bg) {
        double l1 = luminance(fg);
        double l2 = luminance(bg);
        double hi = Math.max(l1, l2);
        double lo = Math.min(l1, l2);
        return (hi + 0.05) / (lo + 0.05);
    }

    /** True when the background counts as white/light. */
    public static boolean isLight(Color bg) {
        if (bg == null) return false;
        return luminance(bg) > 0.30;
    }

    /** Primary text color that contrasts with the given background. */
    public static Color fgFor(Color bg) {
        return isLight(bg) ? LIGHT_PANEL_TEXT : Color.WHITE;
    }

    /** Secondary/muted text color that contrasts with the given background. */
    public static Color mutedFor(Color bg) {
        return isLight(bg) ? LIGHT_PANEL_MUTED : MUTED;
    }

    /** Placeholder color that stays visible on the given background. */
    public static Color placeholderFor(Color bg) {
        return isLight(bg) ? PLACEHOLDER_ON_LIGHT : PLACEHOLDER_ON_DARK;
    }

    /**
     * Returns {@code fg} when it already contrasts with {@code bg},
     * otherwise returns the auto contrasting foreground.
     */
    public static Color ensureFg(Color fg, Color bg) {
        if (fg == null) return fgFor(bg);
        if (bg == null) return fg;
        try {
            if (contrastRatio(fg, bg) >= MIN_CONTRAST) return fg;
        } catch (Exception ignored) { }
        return fgFor(bg);
    }

    /** Effective painted background for a component (walks ancestors). */
    public static Color effectiveBg(Component c) {
        Component cur = c;
        while (cur != null) {
            if (cur instanceof javax.swing.text.JTextComponent
                    || cur instanceof JComboBox
                    || cur instanceof JTable
                    || cur instanceof javax.swing.table.JTableHeader
                    || cur instanceof JPanel
                    || cur instanceof JViewport
                    || cur instanceof JScrollPane
                    || cur instanceof JDialog
                    || cur instanceof JFrame
                    || cur instanceof javax.swing.JWindow) {
                if (cur.getBackground() != null) return cur.getBackground();
            }
            cur = cur.getParent();
        }
        return BG;
    }

    /** Fix a single component's foreground when it fails the contrast rule. */
    public static void fixTextContrast(JComponent c) {
        Color bg = effectiveBg(c);
        if (c instanceof JLabel l) {
            String t = l.getText();
            boolean empty = t == null || t.replaceAll("<[^>]*>", "").isBlank();
            if (!empty) {
                Color fixed = ensureFg(l.getForeground(), bg);
                // Preserve intentional muted/secondary labels only when readable.
                if (l.getForeground() != null && l.getForeground().equals(MUTED)
                        && contrastRatio(MUTED, bg) >= MIN_CONTRAST) return;
                l.setForeground(fixed);
                if (l.getFont() != null && l.getFont().getSize() < 12) {
                    l.setFont(l.getFont().deriveFont(12f));
                }
            }
        } else if (c instanceof AbstractButton b) {
            if (b.getText() != null && !b.getText().isBlank()) {
                b.setForeground(ensureFg(b.getForeground(), b.getBackground() != null ? b.getBackground() : bg));
                if (b.getFont() != null && b.getFont().getSize() < 12) {
                    b.setFont(b.getFont().deriveFont(12f));
                }
            }
            // Disabled text must also stay readable on the same background.
            if (b instanceof JButton jb) {
                jb.setDisabledIcon(null);
            }
            try { b.setDisabledSelectedIcon(null); } catch (Exception ignored) { }
            UIManager.put("Button.disabledText", mutedFor(bg));
        } else if (c instanceof javax.swing.text.JTextComponent t) {
            Color base = t.getBackground() != null ? t.getBackground() : bg;
            t.setForeground(ensureFg(t.getForeground(), base));
            t.setDisabledTextColor(mutedFor(base));
            t.setSelectedTextColor(fgFor(NAV_ACTIVE));
            t.setSelectionColor(NAV_ACTIVE);
            t.setCaretColor(isLight(base) ? INK : BLUE);
            if (t.getFont() != null && t.getFont().getSize() < 12) {
                t.setFont(t.getFont().deriveFont(12f));
            }
        } else if (c instanceof JComboBox<?> cb) {
            Color base = cb.getBackground() != null ? cb.getBackground() : bg;
            cb.setForeground(ensureFg(cb.getForeground(), base));
            if (cb.getFont() != null && cb.getFont().getSize() < 12) {
                cb.setFont(cb.getFont().deriveFont(12f));
            }
        } else if (c instanceof JTable t) {
            Color base = t.getBackground() != null ? t.getBackground() : bg;
            t.setForeground(ensureFg(t.getForeground(), base));
            t.setSelectionForeground(Color.WHITE);
            t.setSelectionBackground(NAV_ACTIVE);
            if (t.getTableHeader() != null) {
                javax.swing.table.JTableHeader h = t.getTableHeader();
                Color hbg = h.getBackground() != null ? h.getBackground() : base;
                h.setForeground(ensureFg(h.getForeground(), hbg));
                if (h.getFont() != null && h.getFont().getSize() < 12) {
                    h.setFont(h.getFont().deriveFont(Font.BOLD, 12f));
                }
            }
        }
    }

    /**
     * Recursively enforces the global contrast rule over a whole
     * screen/panel without touching layout. Safe to call at the end
     * of every panel/frame constructor and after dynamic refreshes.
     */
    public static void fixHierarchy(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JComponent jc) {
                String cn = c.getClass().getSimpleName();
                boolean lafChrome = cn.contains("ArrowButton") || cn.contains("ComboBoxButton");
                if (!lafChrome && !(c instanceof JScrollBar)) {
                    fixTextContrast(jc);
                }
            }
            if (c instanceof Container cont) fixHierarchy(cont);
            // JComboBox popup rows render through the UI delegate; the
            // renderer installed in styleCombo already guarantees contrast.
        }
        // Table headers are not descendants of the table — fix them too.
        for (Component c : root.getComponents()) {
            if (c instanceof JScrollPane sp) {
                Component v = sp.getViewport() != null ? sp.getViewport().getView() : null;
                if (v instanceof JTable t && t.getTableHeader() != null) {
                    Color hbg = t.getTableHeader().getBackground();
                    t.getTableHeader().setForeground(ensureFg(t.getTableHeader().getForeground(), hbg));
                }
            }
            if (c instanceof Container cont) fixTableHeaders(cont);
        }
    }

    private static void fixTableHeaders(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JScrollPane sp) {
                Component v = sp.getViewport() != null ? sp.getViewport().getView() : null;
                if (v instanceof JTable t && t.getTableHeader() != null) {
                    Color hbg = t.getTableHeader().getBackground();
                    t.getTableHeader().setForeground(ensureFg(t.getTableHeader().getForeground(), hbg));
                }
            }
            if (c instanceof Container cont) fixTableHeaders(cont);
        }
    }

    /** Small uppercase section label. */
    public static JLabel label(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(LABEL);
        l.setForeground(MUTED);
        return l;
    }

    /** Rounded dark card panel. */
    public static JPanel card() {
        RoundedPanel p = new RoundedPanel(CARD, BORDER, 16);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        return p;
    }

    /** Compact dark text field. */
    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(CARD_2);
        f.setForeground(fgFor(CARD_2));
        f.setCaretColor(BLUE);
        f.setDisabledTextColor(mutedFor(CARD_2));
        f.setSelectedTextColor(Color.WHITE);
        f.setSelectionColor(NAV_ACTIVE);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER, 10),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return f;
    }

    public static JPasswordField passField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(CARD_2);
        f.setForeground(fgFor(CARD_2));
        f.setCaretColor(BLUE);
        f.setDisabledTextColor(mutedFor(CARD_2));
        f.setSelectedTextColor(Color.WHITE);
        f.setSelectionColor(NAV_ACTIVE);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER, 10),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return f;
    }

    public static JButton primaryButton(String text) {
        return accentButton(text, PURPLE_DEEP, PURPLE, Color.WHITE);
    }

    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(CARD_2);
        b.setForeground(ensureFg(BLUE, CARD_2));
        b.setDisabledIcon(null);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new RoundedBorder(BLUE, 12));
        b.setPreferredSize(new Dimension(150, 38));
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        hover(b, CARD_2, new Color(0x232E4A));
        return b;
    }

    /** Brand-tinted rounded button (e.g. OPEN AMAZON). */
    public static JButton brandButton(String text, Color accent) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        Color bg = new Color(0x1E2740);
        b.setBackground(bg);
        b.setForeground(ensureFg(accent, bg));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new RoundedBorder(accent, 12));
        b.setPreferredSize(new Dimension(150, 34));
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        hover(b, bg, new Color(0x273252));
        return b;
    }

    private static JButton accentButton(String text, Color base, Color hoverC, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? base.darker() : getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // subtle neon glow
                g2.setColor(new Color(PURPLE.getRed(), PURPLE.getGreen(), PURPLE.getBlue(), 60));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(base);
        b.setForeground(ensureFg(fg, base));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setPreferredSize(new Dimension(170, 40));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hoverC); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(base); }
        });
        return b;
    }

    private static void hover(AbstractButton b, Color base, Color hoverC) {
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hoverC); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(base); }
        });
    }

    /** Sidebar navigation item with active glow. */
    public static JToggleButton navButton(String glyph, String text) {
        JToggleButton b = new JToggleButton("  " + glyph + "   " + text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBackground(SIDEBAR);
        b.setForeground(MUTED);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11, 18, 11, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        return b;
    }

    public static void styleNav(JToggleButton b, boolean active) {
        if (active) {
            b.setBackground(NAV_ACTIVE);
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, PURPLE),
                new EmptyBorder(11, 15, 11, 18)));
        } else {
            b.setBackground(SIDEBAR);
            b.setForeground(MUTED);
            b.setBorder(new EmptyBorder(11, 18, 11, 18));
        }
    }

    /** Dark header band color shared by every table (never a white box). */
    public static final Color HEADER_BG = new Color(0x1E2740);

    public static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD);
        table.setForeground(ensureFg(TEXT, CARD));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(HEADER_BG);
        // Header must be maximum contrast: white on the dark header band.
        table.getTableHeader().setForeground(ensureFg(Color.WHITE, HEADER_BG));
        table.getTableHeader().setOpaque(true);
        // Explicit renderer: some native look-and-feels paint their own
        // white header skin and ignore the background property, which hides
        // the header text. This keeps the exact same text, only dark colors.
        table.getTableHeader().setDefaultRenderer(darkHeaderRenderer());
        table.setSelectionBackground(NAV_ACTIVE);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER);
    }

    /**
     * Header renderer that always paints the dark header band with
     * contrasting text. The header text itself is never altered.
     */
    private static javax.swing.table.TableCellRenderer darkHeaderRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean sel,
                                                           boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, sel, foc, row, col);
                javax.swing.table.JTableHeader hh = tbl != null ? tbl.getTableHeader() : null;
                Color bg = (hh != null && hh.getBackground() != null) ? hh.getBackground() : HEADER_BG;
                setBackground(bg);
                Color fg = (hh != null && hh.getForeground() != null) ? hh.getForeground() : Color.WHITE;
                setForeground(ensureFg(fg, bg));
                setOpaque(true);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                return this;
            }
        };
    }

    /** Dark combo box with white text and readable popup (never dark-on-dark). */
    public static <T> void styleCombo(JComboBox<T> c) {
        // The native Windows look-and-feel paints its own white combo skin
        // and ignores background properties, hiding the selected value.
        // A basic UI delegate honors our dark colors; text and behavior stay identical.
        c.setUI(new DarkComboUI());
        c.setBackground(CARD_2);
        c.setForeground(ensureFg(Color.WHITE, CARD_2));
        c.setOpaque(true);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                Color bg = sel ? NAV_ACTIVE : CARD_2;
                setBackground(bg);
                setForeground(ensureFg(Color.WHITE, bg));
                setOpaque(true);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    /** Combo delegate that keeps the dark theme (dark field + dark arrow button). */
    private static class DarkComboUI extends javax.swing.plaf.basic.BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton b = new javax.swing.plaf.basic.BasicArrowButton(
                javax.swing.SwingConstants.SOUTH, CARD_2, CARD_2, Color.WHITE, CARD_2);
            b.setBackground(CARD_2);
            b.setForeground(Color.WHITE);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            return b;
        }
    }

    /**
     * Shared readable cell renderer (pure Swing): picks a contrasting
     * foreground for the row's actual background, for normal, selected
     * and light-background rows. Install once per table; layout untouched.
     */
    public static void installReadableCellRenderer(JTable table) {
        javax.swing.table.DefaultTableCellRenderer r =
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value,
                        boolean sel, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                    Color bg = sel ? t.getSelectionBackground() : t.getBackground();
                    if (bg == null) bg = CARD;
                    setBackground(bg);
                    Color want = sel ? t.getSelectionForeground() : t.getForeground();
                    setForeground(ensureFg(want, bg));
                    return this;
                }
            };
        table.setDefaultRenderer(Object.class, r);
    }

    /** Marketplace letter badge. */
    public static JLabel badge(String letter, Color accent) {
        JLabel l = new JLabel(letter, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(Color.WHITE);
        l.setOpaque(true);
        l.setBackground(new Color(0x1E2740));
        l.setBorder(new RoundedBorder(accent, 10));
        l.setPreferredSize(new Dimension(44, 44));
        return l;
    }

    /** Panel with rounded filled background. */
    public static class RoundedPanel extends JPanel {
        private final Color fill;
        private final Color edge;
        private final int arc;

        public RoundedPanel(Color fill, Color edge, int arc) {
            this.fill = fill;
            this.edge = edge;
            this.arc = arc;
            setOpaque(false);
            setBackground(fill);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(edge);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
        }
    }

    /** Rounded line border. */
    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int arc;

        public RoundedBorder(Color color, int arc) {
            this.color = color;
            this.arc = arc;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(4, 10, 4, 10);
        }
    }

    /** Vertical gradient panel (purple -> navy) for headers/brand areas. */
    public static class GradientPanel extends JPanel {
        private final Color top;
        private final Color bottom;

        public GradientPanel(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bottom);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
