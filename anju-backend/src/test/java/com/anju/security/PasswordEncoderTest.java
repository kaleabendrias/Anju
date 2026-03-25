package com.anju.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;
    private BCryptPasswordEncoder bCryptEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new PasswordEncoder();
        bCryptEncoder = new BCryptPasswordEncoder(10);
    }

    @Nested
    @DisplayName("Password Encoding Tests")
    class PasswordEncodingTests {

        @Test
        @DisplayName("Should encode password with BCrypt strength 10")
        void shouldEncodePasswordWithBCryptStrength10() {
            String rawPassword = "TestPassword123";
            String encodedPassword = passwordEncoder.encode(rawPassword);

            assertNotNull(encodedPassword);
            assertNotEquals(rawPassword, encodedPassword);
            assertTrue(encodedPassword.startsWith("$2a$10$"));
        }

        @Test
        @DisplayName("Should generate different hashes for same password")
        void shouldGenerateDifferentHashesForSamePassword() {
            String rawPassword = "TestPassword123";
            String hash1 = passwordEncoder.encode(rawPassword);
            String hash2 = passwordEncoder.encode(rawPassword);

            assertNotEquals(hash1, hash2);
            assertTrue(passwordEncoder.matches(rawPassword, hash1));
            assertTrue(passwordEncoder.matches(rawPassword, hash2));
        }

        @Test
        @DisplayName("Should correctly verify matching password")
        void shouldCorrectlyVerifyMatchingPassword() {
            String rawPassword = "MySecurePassword123";
            String encodedPassword = passwordEncoder.encode(rawPassword);

            assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        }

        @Test
        @DisplayName("Should correctly verify non-matching password")
        void shouldCorrectlyVerifyNonMatchingPassword() {
            String rawPassword = "MySecurePassword123";
            String wrongPassword = "WrongPassword123";
            String encodedPassword = passwordEncoder.encode(rawPassword);

            assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword));
        }

        @Test
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters() {
            String rawPassword = "P@$$w0rd!#$%^&*()";
            String encodedPassword = passwordEncoder.encode(rawPassword);

            assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            String rawPassword = "密码Password123";
            String encodedPassword = passwordEncoder.encode(rawPassword);

            assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        }
    }

    @Nested
    @DisplayName("BCrypt Strength Tests")
    class BCryptStrengthTests {

        @Test
        @DisplayName("Should use BCrypt strength 10")
        void shouldUseBCryptStrength10() {
            String password = "TestPassword123";
            String encoded = passwordEncoder.encode(password);

            assertTrue(encoded.startsWith("$2a$10$"));
        }

        @Test
        @DisplayName("Encoded password should be 60 characters long")
        void encodedPasswordShouldBe60CharactersLong() {
            String password = "TestPassword123";
            String encoded = passwordEncoder.encode(password);

            assertEquals(60, encoded.length());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty password")
        void shouldHandleEmptyPassword() {
            String encoded = passwordEncoder.encode("");
            assertNotNull(encoded);
            assertTrue(passwordEncoder.matches("", encoded));
        }

        @Test
        @DisplayName("Should handle long password")
        void shouldHandleLongPassword() {
            String longPassword = "A".repeat(200) + "12345678";
            String encoded = passwordEncoder.encode(longPassword);
            assertTrue(passwordEncoder.matches(longPassword, encoded));
        }

        @Test
        @DisplayName("Should handle very short password meeting requirements")
        void shouldHandleVeryShortPasswordMeetingRequirements() {
            String shortPassword = "Ab123456";
            String encoded = passwordEncoder.encode(shortPassword);
            assertTrue(passwordEncoder.matches(shortPassword, encoded));
        }
    }
}
