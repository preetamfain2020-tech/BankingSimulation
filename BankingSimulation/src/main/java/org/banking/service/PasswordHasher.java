package org.banking.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
// import java.security.SecureRandom;
import java.util.Base64;

// Simple password hashing utility
public class PasswordHasher {

    // Hashes a password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not hash password", e);
        }
    }

    // Checks if a plain text password matches a stored hash
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        String hashOfPlainPassword = hashPassword(plainPassword);
        return hashOfPlainPassword.equals(hashedPassword);
    }
}