package com.raceauditor.ui.login;

import com.raceauditor.util.UiTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SplashFrame extends JWindow {
    private final JProgressBar bar = new JProgressBar(0, 100);

    public SplashFrame() {
        setSize(520, 320);
        setLocationRelativeTo(null);
        UiTheme.GradientPanel root = new UiTheme.GradientPanel(new Color(0x1B1440), UiTheme.BG_DEEP);
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(UiTheme.PURPLE, 1));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(60, 50, 20, 50));

        JLabel logo = new JLabel("RACE AUDITOR", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("EXPENSE INTELLIGENCE  •  PRICE RADAR", SwingConstants.CENTER);
        sub.setFont(UiTheme.LABEL);
        sub.setForeground(UiTheme.BLUE);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(logo);
        center.add(Box.createVerticalStrut(8));
        center.add(sub);
        center.add(Box.createVerticalStrut(34));

        bar.setValue(10);
        bar.setStringPainted(true);
        bar.setString("Loading workspace...");
        bar.setForeground(UiTheme.PURPLE);
        bar.setBackground(new Color(0x1E2740));
        bar.setFont(UiTheme.SMALL);
        bar.setBorder(BorderFactory.createEmptyBorder());
        center.add(bar);

        root.add(center, BorderLayout.CENTER);
        JLabel foot = new JLabel("Dark neon edition  •  Swing + MySQL", SwingConstants.CENTER);
        foot.setFont(UiTheme.SMALL);
        foot.setForeground(UiTheme.MUTED);
        root.add(foot, BorderLayout.SOUTH);
        setContentPane(root);
        UiTheme.fixHierarchy(root);

        new Thread(() -> {
            for (int i = 10; i < 95; i += 8) {
                try { Thread.sleep(120); } catch (InterruptedException ignored) {}
                final int v = i;
                SwingUtilities.invokeLater(() -> bar.setValue(v));
            }
        }).start();
    }

    public void setProgress(int v) {
        SwingUtilities.invokeLater(() -> bar.setValue(v));
    }
}
