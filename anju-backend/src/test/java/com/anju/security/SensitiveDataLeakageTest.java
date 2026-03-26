package com.anju.security;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-encryption")
@DisplayName("Sensitive Data Leakage Tests")
class SensitiveDataLeakageTest {

    @Autowired
    private SecureDataMasker secureDataMasker;

    @Autowired
    private FieldEncryptionService fieldEncryptionService;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(SecureDataMasker.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Logger serviceLogger = (Logger) LoggerFactory.getLogger(FieldEncryptionService.class);
        serviceLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        listAppender.stop();
    }

    @Test
    @DisplayName("Passwords should not appear in logs")
    void passwordsShouldNotAppearInLogs() {
        String password = "Anju@1234Secret";
        String masked = secureDataMasker.maskPassword(password);

        assertNotEquals(password, masked);
        assertFalse(listAppender.list.stream()
                .anyMatch(e -> e.getMessage().contains(password)));
    }

    @Test
    @DisplayName("JWT secrets should not appear in logs")
    void jwtSecretsShouldNotAppearInLogs() {
        String jwtSecret = "super_secret_jwt_key_for_testing_only_256_bits_long_key";
        String masked = secureDataMasker.maskSecret(jwtSecret);

        assertNotEquals(jwtSecret, masked);
        assertFalse(listAppender.list.stream()
                .anyMatch(e -> e.getMessage().contains(jwtSecret)));
    }

    @Test
    @DisplayName("ID numbers should be masked in logs")
    void idNumbersShouldBeMaskedInLogs() {
        String idNumber = "110101199001011234";
        String masked = secureDataMasker.maskIdNumber(idNumber);

        assertNotEquals(idNumber, masked);
        assertTrue(masked.contains("*"));
    }

    @Test
    @DisplayName("Phone numbers should be masked in logs")
    void phoneNumbersShouldBeMaskedInLogs() {
        String phone = "13800138000";
        String masked = secureDataMasker.maskPhoneNumber(phone);

        assertNotEquals(phone, masked);
        assertTrue(masked.contains("*"));
    }



    @Test
    @DisplayName("Encrypted field values should not expose raw data")
    void encryptedFieldsShouldNotExposeRawData() {
        String sensitiveData = "SensitiveUserData123";
        String encrypted = fieldEncryptionService.encrypt(sensitiveData);

        assertNotEquals(sensitiveData, encrypted);

        assertFalse(listAppender.list.stream()
                .anyMatch(e -> e.getFormattedMessage().contains(sensitiveData)));
    }

    @Test
    @DisplayName("Encryption service should not log keys")
    void encryptionServiceShouldNotLogKeys() {
        fieldEncryptionService.encrypt("test_data");

        assertFalse(listAppender.list.stream()
                .anyMatch(e -> e.getFormattedMessage().contains("AES") && 
                              e.getFormattedMessage().contains("key")));
    }

    @Test
    @DisplayName("Should mask credit card numbers")
    void shouldMaskCreditCardNumbers() {
        String creditCard = "6222021234567890";
        String masked = secureDataMasker.maskCreditCard(creditCard);

        assertNotEquals(creditCard, masked);
        assertTrue(masked.contains("*"));
        assertTrue(masked.endsWith("7890"));
    }

    @Test
    @DisplayName("Should mask bank account numbers")
    void shouldMaskBankAccountNumbers() {
        String bankAccount = "6222020012345678901";
        String masked = secureDataMasker.maskBankAccount(bankAccount);

        assertNotEquals(bankAccount, masked);
        assertTrue(masked.contains("*"));
    }

    @Test
    @DisplayName("Exception messages should not expose sensitive data")
    void exceptionMessagesShouldNotExposeSensitiveData() {
        try {
            throw new RuntimeException("Password attempt failed: Anju@1234Secret");
        } catch (RuntimeException e) {
            String maskedMessage = secureDataMasker.maskPassword(e.getMessage());
            assertFalse(maskedMessage.contains("Anju@1234Secret"));
        }
    }
}
