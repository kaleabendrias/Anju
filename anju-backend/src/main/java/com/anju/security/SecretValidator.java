package com.anju.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecretValidator {

    private static final int MIN_JWT_SECRET_LENGTH = 64;
    private static final int MIN_ENCRYPTION_KEY_LENGTH = 16;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${security.encryption.key:}")
    private String encryptionKey;

    @Value("${jwt.secret.override:false}")
    private boolean jwtSecretOverride;

    @Value("${security.fail-on-missing-secrets:true}")
    private boolean failOnMissingSecrets;

    @Getter
    private boolean jwtSecretValid = false;

    @Getter
    private boolean encryptionKeyValid = false;

    @PostConstruct
    public void validateSecrets() {
        validateJwtSecret();
        validateEncryptionKey();
        
        if (failOnMissingSecrets) {
            if (!jwtSecretValid || !encryptionKeyValid) {
                throw new SecurityConfigurationException(
                    "Critical security configuration error: " +
                    "JWT secret valid: " + jwtSecretValid + ", " +
                    "Encryption key valid: " + encryptionKeyValid
                );
            }
        }
        
        log.info("Secret validation completed: JWT={}, Encryption={}", 
                jwtSecretValid ? "VALID" : "INVALID",
                encryptionKeyValid ? "VALID" : "INVALID");
    }

    private void validateJwtSecret() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            log.error("JWT secret is not configured");
            jwtSecretValid = false;
            return;
        }

        if (jwtSecretOverride) {
            log.warn("JWT secret override is enabled - using provided secret");
            jwtSecretValid = true;
            return;
        }

        if (jwtSecret.contains("secret") || jwtSecret.contains("default") || 
            jwtSecret.contains("test") || jwtSecret.contains("example")) {
            log.error("JWT secret contains weak/insecure keywords");
            jwtSecretValid = false;
            return;
        }

        if (jwtSecret.length() < MIN_JWT_SECRET_LENGTH) {
            log.error("JWT secret length {} is less than minimum required {}", 
                    jwtSecret.length(), MIN_JWT_SECRET_LENGTH);
            jwtSecretValid = false;
            return;
        }

        if (!containsMixedCase(jwtSecret) || !containsDigit(jwtSecret)) {
            log.error("JWT secret does not meet complexity requirements");
            jwtSecretValid = false;
            return;
        }

        jwtSecretValid = true;
    }

    private void validateEncryptionKey() {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            log.warn("Encryption key is not configured - using auto-generated key");
            encryptionKeyValid = true;
            return;
        }

        if (encryptionKey.length() < MIN_ENCRYPTION_KEY_LENGTH) {
            log.error("Encryption key length {} is less than minimum {}", 
                    encryptionKey.length(), MIN_ENCRYPTION_KEY_LENGTH);
            encryptionKeyValid = false;
            return;
        }

        encryptionKeyValid = true;
    }

    private boolean containsMixedCase(String str) {
        boolean hasUpper = false;
        boolean hasLower = false;
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (hasUpper && hasLower) return true;
        }
        return false;
    }

    private boolean containsDigit(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) return true;
        }
        return false;
    }

    public static class SecurityConfigurationException extends RuntimeException {
        public SecurityConfigurationException(String message) {
            super(message);
        }
    }
}
