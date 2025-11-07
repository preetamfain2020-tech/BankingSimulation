package org.banking.util;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {

    private static final String TRANSACTION_LOG_DIR = "bank_reports/";

    static {
        File dir = new File(TRANSACTION_LOG_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    // Log a deposit/withdrawal/transfer transaction
    public static void logTransaction(String accountNumber, String type, BigDecimal amount, BigDecimal balance) {
        String filePath = TRANSACTION_LOG_DIR + accountNumber + "_transactions.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.printf("%s\t%-15s\t%12.2f\t%12.2f%n", timestamp, type, amount, balance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Generate/update account summary
    public static void generateAccountSummary(String name, String accountNumber, BigDecimal balance) {
        String filePath = TRANSACTION_LOG_DIR + accountNumber + "_summary.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("========== ACCOUNT SUMMARY ==========");
            writer.printf("Holder Name : %s%n", name);
            writer.printf("Account No  : %s%n", accountNumber);
            writer.printf("Balance     : %.2f%n", balance);
            writer.println("=====================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Print account summary in a formatted table
    public static void printAccountSummary(String accountNumber) {
        String filePath = TRANSACTION_LOG_DIR + accountNumber + "_summary.txt";
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No account summary found.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("\n===== ACCOUNT SUMMARY =====");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("===========================\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Print transaction log as a formatted table
    public static void printTransactionLog(String accountNumber) {
        String filePath = TRANSACTION_LOG_DIR + accountNumber + "_transactions.txt";
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No transactions found.");
            return;
        }

        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split("\t"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Print table header
        System.out.printf("%-20s | %-15s | %12s | %12s%n", "Timestamp", "Type", "Amount", "Balance");
        System.out.println("-------------------------------------------------------------------");

        // Print rows
        for (String[] row : rows) {
            if (row.length == 4)
                System.out.printf("%-20s | %-15s | %12s | %12s%n", row[0], row[1], row[2], row[3]);
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    // Low balance alert
    public static void alertLowBalance(String name, String accountNumber, BigDecimal balance, BigDecimal threshold) {
        System.out.println("\n!!! LOW BALANCE ALERT !!!");
        System.out.printf("Account Holder: %s%n", name);
        System.out.printf("Account Number: %s%n", accountNumber);
        System.out.printf("Current Balance: %.2f (Threshold: %.2f)%n", balance, threshold);
        System.out.println("Please top-up your account or contact support.\n");
    }
}
