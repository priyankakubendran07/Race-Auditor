package com.raceauditor.ui.login;

import com.raceauditor.service.AuthenticationService;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.UiTheme;
import java.awt.*;
import javax.swing.*;

public class RegisterDialog extends JDialog {
    private final JTextField name = UiTheme.field(20);
    private final JTextField email = UiTheme.field(20);
    private final JTextField username = UiTheme.field(20);
    private final JPasswordField pass = UiTheme.passField(20);
    private final JPasswordField confirm = UiTheme.passField(20);
    private final AuthenticationService auth = new AuthenticationService();

    public RegisterDialog(Frame owner) {
        super(owner, "Create Account — Race Auditor", true);
        setSize(430, 540);
        setMinimumSize(new Dimension(400, 500));
        setLocationRelativeTo(owner);
        JPanel p = new JPanel();
        p.setBackground(UiTheme.BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));

        JLabel h = new JLabel("Create account");
        h.setFont(UiTheme.HEADING);
        h.setForeground(Color.WHITE);
        JLabel s = new JLabel("Join Race Auditor in seconds");
        s.setFont(UiTheme.SMALL);
        s.setForeground(UiTheme.MUTED);
        p.add(h);
        p.add(s);
        p.add(Box.createVerticalStrut(14));
        p.add(UiTheme.label("Full Name")); p.add(name);
        p.add(Box.createVerticalStrut(8));
        p.add(UiTheme.label("Email")); p.add(email);
        p.add(Box.createVerticalStrut(8));
        p.add(UiTheme.label("Username")); p.add(username);
        p.add(Box.createVerticalStrut(8));
        p.add(UiTheme.label("Password")); p.add(pass);
        p.add(Box.createVerticalStrut(8));
        p.add(UiTheme.label("Confirm Password")); p.add(confirm);
        p.add(Box.createVerticalStrut(18));

        JButton btn = UiTheme.primaryButton("Register");
        btn.addActionListener(e -> doRegister());
        p.add(btn);
        p.add(Box.createVerticalStrut(10));
        JButton back = UiTheme.ghostButton("Back to Login");
        back.addActionListener(e -> dispose());
        p.add(back);
        add(new JScrollPane(p));
        UiTheme.fixHierarchy(p);
    }

    private void doRegister() {
        try {
            auth.register(name.getText(), email.getText(), username.getText(),
                new String(pass.getPassword()), new String(confirm.getPassword()));
            Dialogs.info(this, "Account created! You can now log in.");
            dispose();
        } catch (IllegalArgumentException ex) {
            Dialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            String m = ex.getMessage() == null ? "" : ex.getMessage();
            if (m.contains("duplicate") || m.contains("unique") || m.contains("Duplicate"))
                Dialogs.error(this, "Username or email is already registered.");
            else Dialogs.error(this, "Registration failed: " + m);
        }
    }
}
