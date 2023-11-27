package com.example.habithero;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

    // Formats the date (you can add more methods for different formats)
    public static String formatDate(String date) {
        // Implement date formatting logic here
        return date; // Placeholder
    }

    // Formats the input as US Dollar
    public static String formatUSD(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(amount);
    }

    // Email Validation
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email field cannot be empty";
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Matcher matcher = Pattern.compile(emailRegex).matcher(email);
        if (!matcher.matches()) {
            return "Invalid email format";
        }
        return "VALID";
    }

    // Password Validation
    public static String validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Password field cannot be empty";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit";
        }
        if (!password.matches(".*[!@#$%^&*+=?-].*")) {
            return "Password must contain at least one special character (!@#$%^&*+=?-)";
        }
        // Add more conditions as needed
        return "VALID";
    }
}
