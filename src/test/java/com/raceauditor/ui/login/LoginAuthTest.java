package com.raceauditor.ui.login;

import com.raceauditor.database.DatabaseManager;
import com.raceauditor.model.User;
import com.raceauditor.service.AuthenticationService;
import com.raceauditor.ui.MainFrame;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * End-to-end authentication verification (steps A–G):
 * buttons wired, registration screen opens, register works,
 * valid login opens the dashboard, invalid login shows an error.
 * Skipped automatically when no display or no database is available.
 */
class LoginAuthTest {

    private static final String NAME = "UI Test User";
    private static final String EMAIL = "uitest_user@raceauditor.local";
    private static final String USERNAME = "uitest_user";
    private static final String PASSWORD = "testpass123";

    private static boolean ready;

    @BeforeAll
    static void setUp() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "No display available");
        try (Connection c = DatabaseManager.getConnection()) {
            assumeTrue(c != null && c.isValid(5), "MySQL not reachable");
        } catch (Exception e) {
            abort("MySQL not reachable: " + e.getMessage());
        }
        deleteTestUser();
        AuthenticationService auth = new AuthenticationService();
        auth.register(NAME, EMAIL, USERNAME, PASSWORD, PASSWORD);
        ready = true;
    }

    @AfterAll
    static void tearDown() throws Exception {
        for (Window w : Window.getWindows()) w.dispose();
        if (ready) deleteTestUser();
    }

    private static void deleteTestUser() {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE username=? OR email=?")) {
            ps.setString(1, USERNAME);
            ps.setString(2, EMAIL);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    @Test
    void loginAndCreateAccountButtonsAreWired() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LoginFrame login = new LoginFrame(null);
            try {
                assertTrue(login.getLoginButton().getActionListeners().length > 0,
                    "Login button has no ActionListener");
                assertTrue(login.getCreateAccountButton().getActionListeners().length > 0,
                    "Create Account button has no ActionListener");
            } finally {
                login.dispose();
            }
        });
    }

    @Test
    void createAccountOpensRegistrationScreen() throws Exception {
        LoginFrame login = new LoginFrame(null);
        login.setVisible(true);
        try {
            Thread clicker = new Thread(() -> login.getCreateAccountButton().doClick());
            clicker.start();
            JDialog dialog = waitForDialog("Race Auditor", 10000);
            assertNotNull(dialog, "Clicking Create Account did not open the registration screen");
            SwingUtilities.invokeAndWait(dialog::dispose);
            clicker.join(10000);
        } finally {
            closeAllWindows();
        }
    }

    @Test
    void validLoginOpensDashboard() throws Exception {
        final boolean[] dashboardShown = {false};
        SwingUtilities.invokeAndWait(() -> {
            LoginFrame login = new LoginFrame(null);
            login.setVisible(true);
            login.getLoginField().setText(USERNAME);
            login.getPasswordField().setText(PASSWORD);
            login.getLoginButton().doClick();
            for (Window w : Window.getWindows()) {
                if (w instanceof MainFrame && w.isVisible()) dashboardShown[0] = true;
            }
            assertTrue(dashboardShown[0], "Valid login did not open the dashboard");
            assertFalse(login.isDisplayable(), "Login window was not closed after login");
            for (Window w : Window.getWindows()) w.dispose();
        });
    }

    @Test
    void invalidLoginShowsErrorAndStaysOnLogin() throws Exception {
        LoginFrame login = new LoginFrame(null);
        login.setVisible(true);
        try {
            login.getLoginField().setText(USERNAME);
            login.getPasswordField().setText("wrong-password");
            Thread clicker = new Thread(() -> login.getLoginButton().doClick());
            clicker.start();
            JDialog dialog = waitForDialog("Race Auditor", 10000);
            assertNotNull(dialog, "Invalid login did not show an error message");
            SwingUtilities.invokeAndWait(dialog::dispose);
            clicker.join(10000);
            boolean dashboardShown = false;
            for (Window w : Window.getWindows()) {
                if (w instanceof MainFrame && w.isVisible()) dashboardShown = true;
            }
            assertFalse(dashboardShown, "Invalid login must not open the dashboard");
            assertTrue(login.isDisplayable(), "Login window should stay open after invalid login");
        } finally {
            closeAllWindows();
        }
    }

    private static void closeAllWindows() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (Window w : Window.getWindows()) w.dispose();
        });
    }

    @Test
    void serviceLayerRejectsBadCredentials() {
        AuthenticationService auth = new AuthenticationService();
        assertThrows(IllegalArgumentException.class, () -> auth.login(USERNAME, "wrong-password"));
        assertThrows(IllegalArgumentException.class, () -> auth.login("no_such_user_xyz", "whatever"));
        User u = assertDoesNotThrow(() -> auth.login(USERNAME, PASSWORD));
        assertEquals(USERNAME, u.getUsername());
    }

    @Test
    void serviceLayerRejectsDuplicates() {
        AuthenticationService auth = new AuthenticationService();
        assertThrows(Exception.class,
            () -> auth.register("Someone Else", EMAIL, "someone_else_xyz", "pass1234", "pass1234"));
    }

    private static JDialog waitForDialog(String title, long timeoutMs) throws Exception {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            final JDialog[] found = {null};
            SwingUtilities.invokeAndWait(() -> {
                for (Window w : Window.getWindows()) {
                    if (w instanceof JDialog d && d.getTitle() != null && d.getTitle().contains(title) && d.isVisible()) {
                        found[0] = d;
                    }
                }
            });
            if (found[0] != null) return found[0];
            Thread.sleep(100);
        }
        return null;
    }
}
