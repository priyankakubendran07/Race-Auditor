package com.raceauditor.ui.reports;

import com.raceauditor.dao.ExpenseDAO;
import com.raceauditor.database.DatabaseManager;
import com.raceauditor.model.Expense;
import com.raceauditor.model.User;
import com.raceauditor.service.AuthenticationService;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Verifies the Reports Generate button refreshes totals in real time
 * from the database (no stale values, no silent failure).
 */
class GenerateRefreshTest {

    private static final String USERNAME = "reporttest_user";
    private static final String EMAIL = "reporttest_user@raceauditor.local";

    private static User user;

    @BeforeAll
    static void setUp() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "No display available");
        try (Connection c = DatabaseManager.getConnection()) {
            assumeTrue(c != null && c.isValid(5), "MySQL not reachable");
        } catch (Exception e) {
            abort("MySQL not reachable: " + e.getMessage());
        }
        cleanup();
        user = new AuthenticationService().register(
            "Report Test", EMAIL, USERNAME, "testpass123", "testpass123");
        ExpenseDAO dao = new ExpenseDAO();
        Expense e = new Expense();
        e.setUserId(user.getId());
        e.setDate(LocalDate.now());
        e.setTitle("Audit Verification Expense");
        e.setCategory("Food");
        e.setAmount(1234.56);
        e.setPaymentMethod("UPI");
        e.setNotes("contrast-task verification");
        dao.insert(e);
    }

    @AfterAll
    static void tearDown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (Window w : Window.getWindows()) w.dispose();
        });
        cleanup();
    }

    private static void cleanup() {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM expenses WHERE user_id IN (SELECT id FROM users WHERE username=?)")) {
            ps.setString(1, USERNAME);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE username=? OR email=?")) {
            ps.setString(1, USERNAME);
            ps.setString(2, EMAIL);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    @Test
    void generateButtonRefreshesReportInRealTime() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ReportsPanel panel = new ReportsPanel(user);
            panel.getGenerateButton().doClick();
            String total = panel.getTotalText();
            assertTrue(total != null && total.contains("1,234"),
                "Generate did not refresh the report total in real time (got: '" + total + "')");
        });
        closeAll();
    }

    private static void closeAll() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (Window w : Window.getWindows()) w.dispose();
        });
    }
}
