package com.raceauditor;

import com.raceauditor.database.DatabaseManager;
import com.raceauditor.ui.login.LoginFrame;
import com.raceauditor.ui.login.SplashFrame;
import com.raceauditor.util.UiTheme;
import javax.swing.*;

/** Entry point: splash -> login -> dashboard. Run with: mvn package; java -jar target/race-auditor-1.0.0.jar */
public class Main {
    public static void main(String[] args) {
        UiTheme.apply();
        SwingUtilities.invokeLater(() -> {
            SplashFrame splash = new SplashFrame();
            splash.setVisible(true);
            new Thread(() -> {
                String dbError = null;
                try {
                    DatabaseManager.initSchema();
                } catch (Exception e) {
                    dbError = e instanceof java.sql.SQLException
                        ? DatabaseManager.friendlyError((java.sql.SQLException) e)
                        : "Startup error: " + e.getMessage();
                }
                splash.setProgress(100);
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                splash.dispose();
                final String err = dbError;
                SwingUtilities.invokeLater(() -> {
                    LoginFrame login = new LoginFrame(err);
                    login.setVisible(true);
                });
            }).start();
        });
    }
}
