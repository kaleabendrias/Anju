package com.anju.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Nested
    @DisplayName("Password Complexity Validation Tests")
    class PasswordComplexityTests {

        @Test
        @DisplayName("Should accept valid password with letters and numbers")
        void shouldAcceptValidPassword() {
            assertTrue(passwordValidator.isValid("Password123"));
            assertTrue(passwordValidator.isValid("abc12345"));
            assertTrue(passwordValidator.isValid("Test1234"));
        }

        @Test
        @DisplayName("Should reject password shorter than 8 characters")
        void shouldRejectShortPassword() {
            assertFalse(passwordValidator.isValid("Pass1"));
            assertFalse(passwordValidator.isValid("Ab1"));
            assertFalse(passwordValidator.isValid("1234567"));
        }

        @Test
        @DisplayName("Should reject password without letters")
        void shouldRejectPasswordWithoutLetters() {
            assertFalse(passwordValidator.isValid("12345678"));
            assertFalse(passwordValidator.isValid("123456789"));
        }

        @Test
        @DisplayName("Should reject password without numbers")
        void shouldRejectPasswordWithoutNumbers() {
            assertFalse(passwordValidator.isValid("abcdefgh"));
            assertFalse(passwordValidator.isValid("PasswordOnly"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "        "})
        @DisplayName("Should reject whitespace-only passwords")
        void shouldRejectWhitespaceOnlyPasswords(String password) {
            assertFalse(passwordValidator.isValid(password));
        }

        @Test
        @DisplayName("Should reject null password")
        void shouldRejectNullPassword() {
            assertFalse(passwordValidator.isValid(null));
        }

        @Test
        @DisplayName("Should reject password with only letters (no numbers)")
        void shouldRejectLettersOnlyPassword() {
            assertFalse(passwordValidator.isValid("password"));
            assertFalse(passwordValidator.isValid("PASSWORD"));
            assertFalse(passwordValidator.isValid("Abcdefgh"));
        }

        @Test
        @DisplayName("Should reject password with only numbers (no letters)")
        void shouldRejectNumbersOnlyPassword() {
            assertFalse(passwordValidator.isValid("12345678"));
            assertFalse(passwordValidator.isValid("00000000"));
        }
    }

    @Nested
    @DisplayName("Validation Message Tests")
    class ValidationMessageTests {

        @Test
        @DisplayName("Should return null for valid password")
        void shouldReturnNullForValidPassword() {
            assertNull(passwordValidator.getValidationMessage("Password123"));
        }

        @Test
        @DisplayName("Should return null message for valid password")
        void shouldReturnNullMessageForValidPassword() {
            String message = passwordValidator.getValidationMessage("Valid123");
            assertNull(message);
        }

        @Test
        @DisplayName("Should return length error for short password")
        void shouldReturnLengthErrorForShortPassword() {
            String message = passwordValidator.getValidationMessage("Ab1");
            assertNotNull(message);
            assertTrue(message.contains("at least 8 characters"));
        }

        @Test
        @DisplayName("Should return letter error for password without letters")
        void shouldReturnLetterErrorForPasswordWithoutLetters() {
            String message = passwordValidator.getValidationMessage("12345678");
            assertNotNull(message);
            assertTrue(message.contains("letter"));
        }

        @Test
        @DisplayName("Should return number error for password without numbers")
        void shouldReturnNumberErrorForPasswordWithoutNumbers() {
            String message = passwordValidator.getValidationMessage("Abcdefgh");
            assertNotNull(message);
            assertTrue(message.contains("number"));
        }

        @Test
        @DisplayName("Should return null error for null password")
        void shouldReturnNullErrorForNullPassword() {
            String message = passwordValidator.getValidationMessage(null);
            assertNotNull(message);
            assertTrue(message.contains("cannot be null"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle exactly 8 character valid password")
        void shouldHandleExactlyEightCharacterPassword() {
            assertTrue(passwordValidator.isValid("Abcdef12"));
            assertFalse(passwordValidator.isValid("Abcdef1"));
            assertTrue(passwordValidator.isValid("Abcdef12x"));
        }

        @Test
        @DisplayName("Should handle special characters in password")
        void shouldHandleSpecialCharactersInPassword() {
            assertTrue(passwordValidator.isValid("Pass@123"));
            assertTrue(passwordValidator.isValid("Test#123"));
            assertTrue(passwordValidator.isValid("Pass$123"));
        }

        @Test
        @DisplayName("Should handle ASCII letters only")
        void shouldHandleAsciiLettersOnly() {
            assertTrue(passwordValidator.isValid("Password123"));
            assertTrue(passwordValidator.isValid("password1"));
        }
    }
}
