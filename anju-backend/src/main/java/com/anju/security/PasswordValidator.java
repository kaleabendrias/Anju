package com.anju.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]");

    public boolean isValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = LETTER_PATTERN.matcher(password).find();
        boolean hasNumber = NUMBER_PATTERN.matcher(password).find();
        return hasLetter && hasNumber;
    }

    public String getValidationMessage(String password) {
        if (password == null) {
            return "Password cannot be null";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        boolean hasLetter = LETTER_PATTERN.matcher(password).find();
        boolean hasNumber = NUMBER_PATTERN.matcher(password).find();
        if (!hasLetter) {
            return "Password must contain at least one letter";
        }
        if (!hasNumber) {
            return "Password must contain at least one number";
        }
        return null;
    }
}
