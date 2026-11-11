package com.raceauditor.service;

import com.raceauditor.dao.UserDAO;
import com.raceauditor.model.User;
import com.raceauditor.util.PasswordUtil;
import com.raceauditor.util.Validator;

public class AuthenticationService {
    private final UserDAO dao = new UserDAO();

    public User login(String login, String password) throws Exception {
        if (Validator.isBlank(login) || Validator.isBlank(password)) throw new IllegalArgumentException("Enter username/email and password.");
        User u = dao.findByLogin(login.trim());
        if (u == null) throw new IllegalArgumentException("Invalid login. No account found for '" + login.trim() + "'.");
        String[] creds = dao.credentialsFor(login.trim());
        if (creds == null || !PasswordUtil.verify(password, creds[1], creds[0]))
            throw new IllegalArgumentException("Invalid login. Please check your password.");
        return u;
    }

    public User register(String fullName, String email, String username, String password, String confirm) throws Exception {
        if (Validator.isBlank(fullName) || Validator.isBlank(email) || Validator.isBlank(username)
                || Validator.isBlank(password) || Validator.isBlank(confirm))
            throw new IllegalArgumentException("All fields are required.");
        if (!Validator.isEmail(email.trim())) throw new IllegalArgumentException("Enter a valid email address.");
        if (username.trim().length() < 3) throw new IllegalArgumentException("Username must be at least 3 characters.");
        if (password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (!password.equals(confirm)) throw new IllegalArgumentException("Passwords do not match.");
        if (dao.existsByUsernameOrEmail(username.trim(), email.trim()))
            throw new IllegalArgumentException("Username or email is already registered.");
        return dao.create(fullName.trim(), email.trim(), username.trim(), password);
    }
}
