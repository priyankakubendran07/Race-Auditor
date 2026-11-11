package com.raceauditor.ui.profile;

import com.raceauditor.dao.UserDAO;
import com.raceauditor.model.User;
import com.raceauditor.util.Dialogs;
import com.raceauditor.util.UiTheme;
import com.raceauditor.util.Validator;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProfilePanel extends JPanel {
    private final User user;
    private final UserDAO dao = new UserDAO();
    private final JTextField name = UiTheme.field(20);
    private final JTextField email = UiTheme.field(20);

    public ProfilePanel(User user) {
        this.user = user;
        setBackground(UiTheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(4, 4, 4, 200));
        JLabel h = new JLabel("Profile");
        h.setFont(UiTheme.TITLE);
        h.setForeground(Color.WHITE);
        add(h, BorderLayout.NORTH);

        JPanel col = new JPanel();
        col.setBackground(UiTheme.BG);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        // Identity card
        JPanel id = UiTheme.card();
        id.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));
        JLabel avatar = new JLabel(initial(), SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(UiTheme.PURPLE_DEEP);
        avatar.setPreferredSize(new Dimension(64, 64));
        JPanel who = new JPanel();
        who.setBackground(UiTheme.CARD);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        JLabel nm = new JLabel(user.getFullName());
        nm.setFont(UiTheme.HEADING);
        nm.setForeground(Color.WHITE);
        JLabel un = new JLabel("@" + user.getUsername() + "  •  Member since " + user.getCreatedAt());
        un.setFont(UiTheme.SMALL);
        un.setForeground(UiTheme.MUTED);
        who.add(nm);
        who.add(un);
        id.add(avatar);
        id.add(who);
        col.add(id);
        col.add(Box.createVerticalStrut(12));

        // Edit card
        JPanel card = UiTheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        name.setText(user.getFullName());
        email.setText(user.getEmail());
        card.add(UiTheme.label("Full Name"));
        card.add(name);
        card.add(Box.createVerticalStrut(10));
        card.add(UiTheme.label("Email"));
        card.add(email);
        card.add(Box.createVerticalStrut(10));
        card.add(UiTheme.label("Username (read-only)"));
        JTextField unField = UiTheme.field(20);
        unField.setText(user.getUsername());
        unField.setEditable(false);
        unField.setForeground(UiTheme.MUTED);
        card.add(unField);
        card.add(Box.createVerticalStrut(16));
        JButton save = UiTheme.primaryButton("Save Changes");
        save.addActionListener(e -> doSave());
        card.add(save);
        col.add(card);

        JScrollPane sp = new JScrollPane(col);
        sp.setBackground(UiTheme.BG);
        sp.getViewport().setBackground(UiTheme.BG);
        sp.setBorder(BorderFactory.createEmptyBorder());
        add(sp, BorderLayout.CENTER);
        UiTheme.fixHierarchy(this);
    }

    private String initial() {
        String n = user.getFullName();
        return (n == null || n.isBlank()) ? "R" : n.trim().substring(0, 1).toUpperCase();
    }

    private void doSave() {
        String n = name.getText().trim();
        String e = email.getText().trim();
        if (Validator.isBlank(n) || Validator.isBlank(e)) { Dialogs.error(this, "Name and email are required."); return; }
        if (!Validator.isEmail(e)) { Dialogs.error(this, "Enter a valid email address."); return; }
        try {
            dao.updateProfile(user.getId(), n, e);
            user.setFullName(n);
            user.setEmail(e);
            Dialogs.info(this, "Profile updated.");
        } catch (Exception ex) {
            String m = ex.getMessage() == null ? "" : ex.getMessage();
            if (m.contains("duplicate") || m.contains("unique")) Dialogs.error(this, "That email is already in use.");
            else Dialogs.error(this, "Update failed: " + m);
        }
    }
}
