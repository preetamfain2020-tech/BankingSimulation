package org.banking.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * Sends HTML emails via SMTP configured in src/main/resources/config.properties.
 * - sendLowBalance(...)           : triggered when balance < threshold
 * - sendInsufficientFunds(...)    : triggered when a transaction is denied due to min-balance rule
 */
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private static final Properties cfg = new Properties();
    private static Session session;
    private static boolean enabled = false;
    private static String fromAddr = "";

    static {
        try (InputStream in = EmailService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) cfg.load(in);

            enabled  = Boolean.parseBoolean(cfg.getProperty("mail.enabled", "false"));
            fromAddr = cfg.getProperty("mail.from", "");

            Properties p = new Properties();
            p.put("mail.smtp.auth", "true");
            p.put("mail.smtp.starttls.enable", cfg.getProperty("mail.starttls", "true"));
            p.put("mail.smtp.host", cfg.getProperty("mail.smtp.host", "sandbox.smtp.mailtrap.io"));
            p.put("mail.smtp.port", cfg.getProperty("mail.smtp.port", "587"));
            // extra compatibility
            p.put("mail.smtp.ssl.protocols", "TLSv1.2");
            p.put("mail.smtp.ssl.trust", cfg.getProperty("mail.smtp.host", "sandbox.smtp.mailtrap.io"));

            final String user = cfg.getProperty("mail.username", "");
            final String pass = cfg.getProperty("mail.password", "");

            session = Session.getInstance(p, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            logger.info("EmailService initialized (enabled={}, host={}, from={})",
                    enabled, p.getProperty("mail.smtp.host"), fromAddr);

        } catch (Exception e) {
            logger.error("EmailService initialization failed", e);
        }
    }

    // ----------------- PUBLIC API -----------------

    public static void sendLowBalance(String toEmail, String accountNo,
                                      BigDecimal balance, BigDecimal threshold) {
        if (!enabled) {
            logger.info("Email disabled; skipping low-balance email for acc {}", accountNo);
            return;
        }
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddr));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject("Low Balance Alert — Account " + maskAccount(accountNo));
            msg.setContent(buildLowBalanceHtml(maskAccount(accountNo), balance, threshold), "text/html; charset=UTF-8");
            Transport.send(msg);
            logger.info("Low-balance email sent to {} for account {}", toEmail, accountNo);
        } catch (Exception e) {
            logger.error("Failed to send low-balance email for acc {}", accountNo, e);
        }
    }

    public static void sendInsufficientFunds(String toEmail, String accountNo,
                                             BigDecimal currentBalance, BigDecimal threshold,
                                             BigDecimal attemptedAmount) {
        if (!enabled) {
            logger.info("Email disabled; skipping insufficient-funds email for acc {}", accountNo);
            return;
        }
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddr));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject("Transaction Denied — Minimum Balance Policy (" + maskAccount(accountNo) + ")");
            msg.setContent(buildDeniedHtml(maskAccount(accountNo), currentBalance, threshold, attemptedAmount), "text/html; charset=UTF-8");
            Transport.send(msg);
            logger.info("Insufficient-funds email sent to {} for account {}", toEmail, accountNo);
        } catch (Exception e) {
            logger.error("Failed to send insufficient-funds email for acc {}", accountNo, e);
        }
    }

    // ----------------- HELPERS -----------------

    private static String maskAccount(String acc) {
        if (acc == null || acc.length() <= 4) return acc;
        return "XXXX-" + acc.substring(Math.max(0, acc.length() - 4));
    }

    private static String buildLowBalanceHtml(String maskedAcc, BigDecimal bal, BigDecimal thr) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String sbal = nf.format(bal);
        String sthr = nf.format(thr);

        return """
            <div style="font-family:Segoe UI,Arial,sans-serif;max-width:560px;margin:0 auto;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden">
              <div style="background:#0b5cff;padding:14px 18px;color:#fff;">
                <h2 style="margin:0;font-weight:600;">Banking Transaction Simulator</h2>
                <div style="opacity:.9;font-size:13px">Automated Balance Notification</div>
              </div>
              <div style="padding:18px 18px 8px 18px;color:#111827;">
                <p style="margin:0 0 12px 0;">Dear Customer,</p>
                <p style="margin:0 0 12px 0;">
                  Your account <strong>%s</strong> has a current balance of
                  <strong>%s</strong>, which is below the minimum threshold of
                  <strong>%s</strong>.
                </p>
                <div style="background:#f9fafb;border:1px solid #e5e7eb;border-radius:10px;padding:12px 14px;margin:14px 0;">
                  <div style="display:flex;justify-content:space-between;margin:6px 0;">
                    <span style="color:#6b7280;">Account</span><span style="font-weight:600;">%s</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;margin:6px 0;">
                    <span style="color:#6b7280;">Current Balance</span><span style="font-weight:600;">%s</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;margin:6px 0;">
                    <span style="color:#6b7280;">Min. Threshold</span><span style="font-weight:600;">%s</span>
                  </div>
                </div>
                <p style="margin:0 0 8px 0;">Please add funds to avoid future transaction denials.</p>
              </div>
              <div style="background:#f3f4f6;padding:12px 18px;color:#6b7280;font-size:12px;text-align:center;">
                © %s Banking Transaction Simulator
              </div>
            </div>
            """.formatted(maskedAcc, sbal, sthr, maskedAcc, sbal, sthr, String.valueOf(java.time.Year.now()));
    }

    private static String buildDeniedHtml(String maskedAcc, BigDecimal bal, BigDecimal thr, BigDecimal attempted) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String sbal = nf.format(bal);
        String sthr = nf.format(thr);
        String satt = nf.format(attempted);

        return """
            <div style="font-family:Segoe UI,Arial,sans-serif;max-width:560px;margin:0 auto;border:1px solid #fde68a;border-radius:12px;overflow:hidden">
              <div style="background:#f59e0b;padding:14px 18px;color:#111;">
                <h2 style="margin:0;font-weight:700;">Transaction Denied</h2>
                <div style="opacity:.85;font-size:13px">Minimum Balance Policy</div>
              </div>
              <div style="padding:18px 18px 8px 18px;color:#111827;">
                <p style="margin:0 0 12px 0;">Dear Customer,</p>
                <p style="margin:0 0 12px 0;">
                  Your recent transaction request of <strong>%s</strong> for account
                  <strong>%s</strong> was <strong>denied</strong> because it would reduce the balance
                  below the minimum required threshold.
                </p>
                <div style="background:#fff7ed;border:1px solid #fed7aa;border-radius:10px;padding:12px 14px;margin:14px 0;">
                  <div style="display:flex;justify-content:space-between;margin:6px 0;">
                    <span style="color:#6b7280;">Account</span><span style="font-weight:600;">%s</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;margin:6px 0;">
                    <span style="color:#6b7280;">Attempted Amount</span><span style="font-weight:600;">%s</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;margin:6px 0;">
                    <span style="color:#6b7280;">Current Balance</span><span style="font-weight:600;">%s</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;margin:6px 0;">
                    <span style="color:#6b7280;">Min. Threshold</span><span style="font-weight:600;">%s</span>
                  </div>
                </div>
                <p style="margin:0 0 8px 0;">Please deposit funds and try again.</p>
              </div>
              <div style="background:#fffbeb;padding:12px 18px;color:#78350f;font-size:12px;text-align:center;">
                © %s Banking Transaction Simulator
              </div>
            </div>
            """.formatted(satt, maskedAcc, maskedAcc, satt, sbal, sthr, String.valueOf(java.time.Year.now()));
    }
}
