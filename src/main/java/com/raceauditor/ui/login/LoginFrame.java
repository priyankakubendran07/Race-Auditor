package com.raceauditor.ui.login;

import com.raceauditor.model.User;
import com.raceauditor.service.AuthenticationService;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.UiTheme;
import com.raceauditor.util.Validator;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private final AuthenticationService auth = new AuthenticationService();
    private final JTextField loginField = UiTheme.field(20);
    private final JPasswordField passField = UiTheme.passField(20);
    private final JButton loginBtn = UiTheme.primaryButton("Login");
    private final JButton regBtn = UiTheme.ghostButton("Create Account");

    public LoginFrame(String dbError) {
        super("Race Auditor — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 580);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UiTheme.BG);
        setLayout(new GridLayout(1, 2));

        // Left brand panel
        UiTheme.GradientPanel brand = new UiTheme.GradientPanel(new Color(0x2A1B5E), new Color(0x0E2A4A));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(90, 52, 90, 52));
        JLabel mark = new JLabel("◈ RACE AUDITOR");
        mark.setFont(new Font("Segoe UI", Font.BOLD, 15));
        mark.setForeground(UiTheme.CYAN);
        JLabel t1 = new JLabel("<html><span style='font-size:34px;color:white'><b>Track money.<br>Beat prices.</b></span></html>");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 34));
        JLabel t2 = new JLabel("<html><p style='color:#B9C2D8;width:260px'>Monthly expenses, category insights and marketplace price comparison in one dark premium workspace.</p></html>");
        t2.setFont(UiTheme.BODY);
        brand.add(mark);
        brand.add(Box.createVerticalStrut(18));
        brand.add(t1);
        brand.add(Box.createVerticalStrut(14));
        brand.add(t2);
        add(brand);

        // Right form panel
        JPanel form = new JPanel();
        form.setBackground(UiTheme.BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(80, 56, 80, 56));

        JLabel h = new JLabel("Welcome back");
        h.setFont(UiTheme.TITLE);
        h.setForeground(Color.WHITE);
        JLabel s = new JLabel("Log in to continue");
        s.setFont(UiTheme.SUBTITLE);
        s.setForeground(UiTheme.MUTED);

        form.add(h);
        form.add(s);
        form.add(Box.createVerticalStrut(26));
        form.add(UiTheme.label("Username or Email"));
        form.add(Box.createVerticalStrut(4));
        form.add(loginField);
        form.add(Box.createVerticalStrut(12));
        form.add(UiTheme.label("Password"));
        form.add(Box.createVerticalStrut(4));
        form.add(passField);
        JCheckBox show = new JCheckBox("Show password");
        show.setBackground(UiTheme.BG);
        show.setForeground(UiTheme.MUTED);
        show.setFont(UiTheme.SMALL);
        show.addActionListener(e -> passField.setEchoChar(show.isSelected() ? (char) 0 : '•'));
        form.add(show);
        form.add(Box.createVerticalStrut(20));

        loginBtn.addActionListener(e -> doLogin());
        regBtn.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setBackground(UiTheme.BG);
        row.add(loginBtn);
        row.add(regBtn);
        form.add(row);
        add(form);

        getRootPane().setDefaultButton(loginBtn);

        UiTheme.fixHierarchy(getContentPane());

        if (dbError != null) {
            SwingUtilities.invokeLater(() -> Dialogs.error(this,
                "MySQL connection failed — the app opened so you can fix settings, but login will not work until the database is reachable.\n\n" + dbError));
        }
    }

    /** Exposed for automated UI verification. */
    JButton getLoginButton() { return loginBtn; }

    /** Exposed for automated UI verification. */
    JButton getCreateAccountButton() { return regBtn; }

    /** Exposed for automated UI verification. */
    JTextField getLoginField() { return loginField; }

    /** Exposed for automated UI verification. */
    JPasswordField getPasswordField() { return passField; }

    private void doLogin() {
        String login = loginField.getText().trim();
        String pass = new String(passField.getPassword());
        if (Validator.isBlank(login) || Validator.isBlank(pass)) {
            Dialogs.error(this, "Enter username/email and password.");
            return;
        }
        try {
            User u = auth.login(login, pass);
            dispose();
            new com.raceauditor.ui.MainFrame(u).setVisible(true);
        } catch (IllegalArgumentException ex) {
            Dialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            Dialogs.error(this, "Database error: " + ex.getMessage());
        }
    }
}
