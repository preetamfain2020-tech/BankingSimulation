package org.banking.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;

public class BalanceAlertMonitor {

    private static final Logger logger = LoggerFactory.getLogger(BalanceAlertMonitor.class);
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "balance-alert-monitor");
        t.setDaemon(true);
        return t;
    });

    // accounts currently below threshold that we already alerted for
    private static final Set<String> alerted = ConcurrentHashMap.newKeySet();

    private static int intervalSeconds = 60; // default

    static {
        try (InputStream in = BalanceAlertMonitor.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                intervalSeconds = Integer.parseInt(p.getProperty("alert.check.interval", "60").trim());
            }
        } catch (Exception e) {
            logger.warn("Could not load alert.check.interval; using default {}s", intervalSeconds, e);
        }
    }

    public static void start() {
        logger.info("Starting BalanceAlertMonitor (interval={}s)…", intervalSeconds);
        EXEC.scheduleAtFixedRate(BalanceAlertMonitor::scanOnce, 5, intervalSeconds, TimeUnit.SECONDS);
    }

    public static void stop() {
        logger.info("Stopping BalanceAlertMonitor…");
        EXEC.shutdown();
        try {
            EXEC.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    private static void scanOnce() {
        String sql = """
            SELECT a.account_number, a.balance, a.min_balance_threshold,
                   c.first_name, c.last_name, c.email
            FROM accounts a
            JOIN customers c ON a.customer_id = c.customer_id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Set<String> stillBelow = new HashSet<>();

            while (rs.next()) {
                String acc = rs.getString("account_number");
                BigDecimal bal = rs.getBigDecimal("balance");
                BigDecimal thr = rs.getBigDecimal("min_balance_threshold");
                String email   = rs.getString("email");
                String name    = rs.getString("first_name") + " " + rs.getString("last_name");

                if (bal == null || thr == null) continue;

                if (bal.compareTo(thr) < 0) {
                    stillBelow.add(acc);

                    // only alert once while it remains below threshold
                    if (!alerted.contains(acc)) {
                        logger.warn("Low balance detected (acc={}, bal={}, thr={}) — sending alert", acc, bal, thr);
                        ReportGenerator.alertLowBalance(name, acc, bal, thr);
                        if (email != null && !email.isBlank()) {
                            EmailService.sendLowBalance(email, acc, bal, thr);
                        }
                        alerted.add(acc);
                    }
                }
            }

            // remove recovered accounts from alerted set
            alerted.removeIf(acc -> !stillBelow.contains(acc));

        } catch (SQLException e) {
            logger.error("BalanceAlertMonitor SQL error", e);
        } catch (Exception e) {
            logger.error("BalanceAlertMonitor error", e);
        }
    }
}
