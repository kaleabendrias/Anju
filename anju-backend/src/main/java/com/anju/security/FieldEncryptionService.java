package com.anju.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class FieldEncryptionService {

    private static final Logger log = LoggerFactory.getLogger(FieldEncryptionService.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int MIN_KEY_LENGTH = 16;
    private static final String KEY_HEX_CHARS = "0123456789ABCDEF";

    private final SecureRandom secureRandom;
    private final Environment environment;
    private SecretKey encryptionKey;
    private boolean encryptionEnabled;
    private boolean properlyInitialized = false;

    @Value("${security.field-encryption.enabled:true}")
    private boolean encryptionConfigEnabled;

    @Value("${security.field-encryption.key:}")
    private String encryptionKeyMaterial;

    public FieldEncryptionService(Environment environment) {
        this.secureRandom = new SecureRandom();
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        this.encryptionEnabled = encryptionConfigEnabled;
        
        boolean isTestProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("test") || 
                                    profile.equalsIgnoreCase("unit") ||
                                    profile.equalsIgnoreCase("dev"));

        if (encryptionKeyMaterial != null && !encryptionKeyMaterial.isEmpty()) {
            if (encryptionKeyMaterial.length() < MIN_KEY_LENGTH) {
                log.error("SECURITY ERROR: Provided encryption key is too short. Minimum length: {} characters", MIN_KEY_LENGTH);
                throw new IllegalStateException(
                        "SECURITY CONFIGURATION ERROR: Encryption key must be at least " + MIN_KEY_LENGTH + " characters long."
                );
            }
            
            this.encryptionKey = deriveKey(encryptionKeyMaterial);
            this.properlyInitialized = true;
            log.info("Field-level encryption initialized with provided key (enabled: {}, keyLength: {})", 
                    encryptionEnabled, encryptionKeyMaterial.length());
        } else {
            if (!isTestProfile) {
                log.error("=".repeat(70));
                log.error("SECURITY CRITICAL FAILURE: Field encryption key is NOT configured.");
                log.error("=".repeat(70));
                log.error("The application is starting in a non-test profile without an encryption key.");
                log.error("This is a SECURITY VIOLATION and the application cannot start.");
                log.error("");
                log.error("REQUIRED ACTION: Set the following environment variable or property:");
                log.error("  security.field-encryption.key=<your-secure-key-minimum-" + MIN_KEY_LENGTH + "-characters>");
                log.error("");
                log.error("Example: export security.field-encryption.key='YourSecureEncryptionKey123!'");
                log.error("=".repeat(70));
                throw new IllegalStateException(
                        "SECURITY CONFIGURATION ERROR: Field encryption key is required in production profiles. " +
                        "Set 'security.field-encryption.key' with a secure key (minimum " + MIN_KEY_LENGTH + " characters). " +
                        "Application startup aborted."
                );
            }
            
            log.warn("Test profile detected - generating temporary encryption key for testing only.");
            byte[] randomKey = new byte[32];
            secureRandom.nextBytes(randomKey);
            this.encryptionKey = new SecretKeySpec(randomKey, "AES");
            this.properlyInitialized = true;
            this.encryptionEnabled = false;
            log.warn("WARNING: Using auto-generated encryption key in test mode. " +
                    "Data encrypted with this key cannot be decrypted in production!");
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        if (!properlyInitialized) {
            throw new IllegalStateException("Encryption service not properly initialized");
        }
        
        if (!encryptionEnabled) {
            return plainText;
        }
        
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec);

            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption operation failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        if (!properlyInitialized) {
            throw new IllegalStateException("Encryption service not properly initialized");
        }
        
        if (!encryptionEnabled) {
            return encryptedText;
        }
        
        if (encryptedText.startsWith("ENC_FAILED:")) {
            log.warn("Cannot decrypt failed encryption placeholder");
            return null;
        }
        
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encryptedData, 0, encryptedData.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed for value: {}", maskValue(encryptedText), e);
            throw new RuntimeException("Decryption operation failed", e);
        }
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public boolean isProperlyInitialized() {
        return properlyInitialized;
    }

    private SecretKey deriveKey(String keyMaterial) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String maskValue(String value) {
        if (value == null || value.length() <= 8) {
            return "***";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    public String generateSecureKey() {
        StringBuilder key = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            key.append(KEY_HEX_CHARS.charAt(secureRandom.nextInt(KEY_HEX_CHARS.length())));
        }
        return key.toString();
    }
}
