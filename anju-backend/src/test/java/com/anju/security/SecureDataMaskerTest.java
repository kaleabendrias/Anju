package com.anju.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecureDataMaskerTest {

    private SecureDataMasker masker;

    @BeforeEach
    void setUp() {
        masker = new SecureDataMasker("test-encryption-key-1234567890");
    }

    @Nested
    @DisplayName("Phone Number Masking Tests")
    class PhoneNumberMaskingTests {

        @Test
        @DisplayName("Should mask valid phone number")
        void shouldMaskValidPhoneNumber() {
            String result = masker.maskPhoneNumber("13812345678");
            
            assertEquals("138****5678", result);
        }

        @Test
        @DisplayName("Should return null for null phone")
        void shouldReturnNullForNullPhone() {
            assertNull(masker.maskPhoneNumber(null));
        }

        @Test
        @DisplayName("Should return null for empty phone")
        void shouldReturnNullForEmptyPhone() {
            assertNull(masker.maskPhoneNumber(""));
        }
    }

    @Nested
    @DisplayName("Email Masking Tests")
    class EmailMaskingTests {

        @Test
        @DisplayName("Should mask valid email")
        void shouldMaskValidEmail() {
            String result = masker.maskEmail("testuser@example.com");
            
            assertEquals("te***@example.com", result);
        }

        @Test
        @DisplayName("Should mask short email local part")
        void shouldMaskShortEmailLocalPart() {
            String result = masker.maskEmail("ab@example.com");
            
            assertEquals("a***@example.com", result);
        }

        @Test
        @DisplayName("Should return null for null email")
        void shouldReturnNullForNullEmail() {
            assertNull(masker.maskEmail(null));
        }
    }

    @Nested
    @DisplayName("ID Number Masking Tests")
    class IdNumberMaskingTests {

        @Test
        @DisplayName("Should mask ID number")
        void shouldMaskIdNumber() {
            String result = masker.maskIdNumber("110101199001011234");
            
            assertEquals("1101****1234", result);
        }

        @Test
        @DisplayName("Should return null for null ID")
        void shouldReturnNullForNullId() {
            assertNull(masker.maskIdNumber(null));
        }
    }

    @Nested
    @DisplayName("General Masking Tests")
    class GeneralMaskingTests {

        @Test
        @DisplayName("Should mask general sensitive field")
        void shouldMaskGeneralSensitiveField() {
            String result = masker.mask("1234567890123456");
            
            assertTrue(result.startsWith("12"));
            assertTrue(result.endsWith("56"));
            assertTrue(result.contains("****"));
        }

        @Test
        @DisplayName("Should mask short strings completely")
        void shouldMaskShortStringsCompletely() {
            String result = masker.mask("abc");
            
            assertEquals("***", result);
        }

        @Test
        @DisplayName("Should return null for null value")
        void shouldReturnNullForNullValue() {
            assertNull(masker.mask(null));
        }
    }

    @Nested
    @DisplayName("Encryption Tests")
    class EncryptionTests {

        @Test
        @DisplayName("Should encrypt sensitive data")
        void shouldEncryptSensitiveData() {
            String encrypted = masker.encryptSensitive("sensitive-data");
            
            assertNotNull(encrypted);
            assertNotEquals("sensitive-data", encrypted);
        }

        @Test
        @DisplayName("Should produce different encryptions for same input")
        void shouldProduceDifferentEncryptions() {
            String encrypted1 = masker.encryptSensitive("same-data");
            String encrypted2 = masker.encryptSensitive("same-data");
            
            assertNotEquals(encrypted1, encrypted2);
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertNull(masker.encryptSensitive(null));
        }
    }

    @Nested
    @DisplayName("Hash Tests")
    class HashTests {

        @Test
        @DisplayName("Should produce consistent SHA-256 hash")
        void shouldProduceConsistentHash() {
            String hash1 = masker.hashSha256("test-data");
            String hash2 = masker.hashSha256("test-data");
            
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("Should produce 64 character hash")
        void shouldProduce64CharacterHash() {
            String hash = masker.hashSha256("test-data");
            
            assertEquals(64, hash.length());
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertNull(masker.hashSha256(null));
        }
    }
}
