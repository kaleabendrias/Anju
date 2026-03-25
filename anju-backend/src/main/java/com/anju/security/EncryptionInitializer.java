package com.anju.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionInitializer {

    private static final Logger log = LoggerFactory.getLogger(EncryptionInitializer.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static SecretKeySpec encryptionKey;
    public static volatile boolean encryptionEnabled = false;
    private static volatile boolean initialized = false;

    @Value("${security.field-encryption.enabled:true}")
    private void setEncryptionEnabled(boolean enabled) {
        encryptionEnabled = enabled;
    }

    @Value("${security.field-encryption.key:}")
    private void setEncryptionKeyMaterial(String keyMaterial) {
        if (!initialized && keyMaterial != null && !keyMaterial.isEmpty()) {
            encryptionKey = deriveKey(keyMaterial);
            initialized = true;
            log.info("Field-level encryption initialized (enabled: {})", encryptionEnabled);
        }
    }

    @PostConstruct
    public void init() {
        if (!initialized) {
            byte[] randomKey = new byte[32];
            SECURE_RANDOM.nextBytes(randomKey);
            encryptionKey = new SecretKeySpec(randomKey, "AES");
            encryptionEnabled = false;
            log.warn("Field-level encryption initialized with auto-generated key - encryption disabled");
        }
    }

    private static SecretKeySpec deriveKey(String keyMaterial) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive encryption key", e);
        }
    }

    public static String encrypt(String attribute) {
        if (attribute == null) {
            return null;
        }
        if (!encryptionEnabled || encryptionKey == null) {
            return attribute;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec);

            byte[] encryptedData = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            return attribute;
        }
    }

    public static String decrypt(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (!encryptionEnabled || encryptionKey == null) {
            return dbData;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);

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
            return dbData;
        }
    }
}
