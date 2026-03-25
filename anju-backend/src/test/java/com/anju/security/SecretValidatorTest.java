package com.anju.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class SecretValidatorTest {

    @Nested
    @DisplayName("Secret Validation Tests")
    class SecretValidationTests {

        @Test
        @DisplayName("Should validate strong JWT secret")
        void shouldValidateStrongJwtSecret() {
            SecretValidator validator = new SecretValidator();
            ReflectionTestUtils.setField(validator, "jwtSecret", "ThisIsAVeryStrongSecretKeyThatIsAtLeast64CharactersLongForSecurity!");
            ReflectionTestUtils.setField(validator, "encryptionKey", "test-encryption-key");
            ReflectionTestUtils.setField(validator, "jwtSecretOverride", false);
            ReflectionTestUtils.setField(validator, "failOnMissingSecrets", false);
            
            validator.validateSecrets();
            
            assertTrue(validator.isJwtSecretValid());
        }

        @Test
        @DisplayName("Should reject weak JWT secret")
        void shouldRejectWeakJwtSecret() {
            SecretValidator validator = new SecretValidator();
            ReflectionTestUtils.setField(validator, "jwtSecret", "weak-secret");
            ReflectionTestUtils.setField(validator, "encryptionKey", "test-key");
            ReflectionTestUtils.setField(validator, "jwtSecretOverride", false);
            ReflectionTestUtils.setField(validator, "failOnMissingSecrets", false);
            
            validator.validateSecrets();
            
            assertFalse(validator.isJwtSecretValid());
        }

        @Test
        @DisplayName("Should reject short JWT secret")
        void shouldRejectShortJwtSecret() {
            SecretValidator validator = new SecretValidator();
            ReflectionTestUtils.setField(validator, "jwtSecret", "ShortSecret123");
            ReflectionTestUtils.setField(validator, "encryptionKey", "test-key");
            ReflectionTestUtils.setField(validator, "jwtSecretOverride", false);
            ReflectionTestUtils.setField(validator, "failOnMissingSecrets", false);
            
            validator.validateSecrets();
            
            assertFalse(validator.isJwtSecretValid());
        }

        @Test
        @DisplayName("Should reject secret with default keywords")
        void shouldRejectSecretWithDefaultKeywords() {
            SecretValidator validator = new SecretValidator();
            ReflectionTestUtils.setField(validator, "jwtSecret", "defaultSecretKey1234567890123456789012345678901234567890123456");
            ReflectionTestUtils.setField(validator, "encryptionKey", "test-key");
            ReflectionTestUtils.setField(validator, "jwtSecretOverride", false);
            ReflectionTestUtils.setField(validator, "failOnMissingSecrets", false);
            
            validator.validateSecrets();
            
            assertFalse(validator.isJwtSecretValid());
        }

        @Test
        @DisplayName("Should validate encryption key")
        void shouldValidateEncryptionKey() {
            SecretValidator validator = new SecretValidator();
            ReflectionTestUtils.setField(validator, "jwtSecret", "ValidSecretKey12345678901234567890123456789012345678901234567");
            ReflectionTestUtils.setField(validator, "encryptionKey", "test-encryption-key");
            ReflectionTestUtils.setField(validator, "jwtSecretOverride", false);
            ReflectionTestUtils.setField(validator, "failOnMissingSecrets", false);
            
            validator.validateSecrets();
            
            assertTrue(validator.isEncryptionKeyValid());
        }

        @Test
        @DisplayName("Should throw exception when failOnMissingSecrets is true and secret is invalid")
        void shouldThrowExceptionWhenFails() {
            SecretValidator validator = new SecretValidator();
            ReflectionTestUtils.setField(validator, "jwtSecret", "weak");
            ReflectionTestUtils.setField(validator, "encryptionKey", "short");
            ReflectionTestUtils.setField(validator, "jwtSecretOverride", false);
            ReflectionTestUtils.setField(validator, "failOnMissingSecrets", true);
            
            assertThrows(SecretValidator.SecurityConfigurationException.class, 
                validator::validateSecrets);
        }
    }
}
