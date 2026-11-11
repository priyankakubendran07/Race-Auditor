package com.raceauditor.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Clean dark dialogs matching the Race Auditor theme.
 * No message icons — text-only, white bold message, clearly visible buttons.
 * Same static API as before, so all call sites work unchanged.
 */
public final class Dialogs {
    private Dialogs() {}

    public static void error(Component parent, String message) {
        show(parent, message, new String[]{"OK"}, 0);
    }

    public static void info(Component parent, String message) {
        show(parent, message, new String[]{"OK"}, 0);
    }

    public static boolean confirm(Component parent, String message) {
        return show(parent, message, new String[]{"Yes", "No"}, 0) == 0;
    }

    /** Builds and shows a modal dark dialog; returns the index of the pressed button. */
    private static int show(Component parent, String message, String[] options, int defaultIdx) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Race Auditor", JDialog.DEFAULT_MODALITY_TYPE);
        dialog.getContentPane().setBackground(UiTheme.BG);

        JPanel root = new JPanel();
        root.setBackground(UiTheme.BG);
        root.setLayout(new javax.swing.BoxLayout(root, javax.swing.BoxLayout.Y_AXIS));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 28, 24, 28));

        String safe = message == null ? "" : message
            .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\n", "<br>");
        JLabel text = new JLabel("<html><p style='width:360px'>" + safe + "</p></html>");
        text.setFont(new Font("Segoe UI", Font.BOLD, 14));
        text.setForeground(java.awt.Color.WHITE);
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(text);
        root.add(Box.createVerticalStrut(20));

        JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        row.setBackground(UiTheme.BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        final int[] result = {defaultIdx};
        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            JButton b = options.length == 1 || i == 0
                ? UiTheme.primaryButton(options[i])
                : UiTheme.ghostButton(options[i]);
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            b.setPreferredSize(new Dimension(120, 40));
            b.addActionListener(e -> {
                result[0] = idx;
                dialog.dispose();
            });
            row.add(b);
            if (i == defaultIdx) dialog.getRootPane().setDefaultButton(b);
        }
        root.add(row);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(440, 160));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }
}
