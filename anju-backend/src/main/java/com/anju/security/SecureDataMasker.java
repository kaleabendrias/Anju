package com.anju.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

@Component
public class SecureDataMasker {

    private static final String MASK_CHAR = "*";
    private static final int VISIBLE_PREFIX = 2;
    private static final int VISIBLE_SUFFIX = 2;
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d{6,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern BANK_ACCOUNT_PATTERN = Pattern.compile("^\\d{10,20}$");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("^\\d{13,19}$");
    private static final Pattern SECRET_PATTERN = Pattern.compile("^[a-zA-Z0-9+/=]{16,}$");

    private final SecureRandom secureRandom;
    private final SecretKey encryptionKey;

    public SecureDataMasker(
            @Value("${security.encryption.key:}") String encryptionKey) {
        this.secureRandom = new SecureRandom();
        this.encryptionKey = deriveKey(encryptionKey);
    }

    public String maskPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        if (PHONE_PATTERN.matcher(phone).matches()) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return mask(phone);
    }

    public String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        if (EMAIL_PATTERN.matcher(email).matches()) {
            int atIndex = email.indexOf('@');
            String localPart = email.substring(0, atIndex);
            String domain = email.substring(atIndex);
            if (localPart.length() <= 3) {
                return localPart.charAt(0) + "***" + domain;
            }
            return localPart.substring(0, 2) + "***" + domain;
        }
        return mask(email);
    }

    public String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.isEmpty()) {
            return null;
        }
        if (ID_PATTERN.matcher(idNumber).matches()) {
            return idNumber.substring(0, 4) + "****" + idNumber.substring(idNumber.length() - 4);
        }
        return mask(idNumber);
    }

    public String maskSensitiveField(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return mask(value);
    }

    public String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        if (password.length() <= 2) {
            return "*".repeat(password.length());
        }
        return password.substring(0, 1) + "*".repeat(Math.min(password.length() - 2, 8)) + password.substring(password.length() - 1);
    }

    public String maskBankAccount(String bankAccount) {
        if (bankAccount == null || bankAccount.isEmpty()) {
            return null;
        }
        if (BANK_ACCOUNT_PATTERN.matcher(bankAccount).matches()) {
            if (bankAccount.length() >= 12) {
                return bankAccount.substring(0, 4) + "****" + bankAccount.substring(bankAccount.length() - 4);
            }
            return "****" + bankAccount.substring(bankAccount.length() - 4);
        }
        return mask(bankAccount);
    }

    public String maskCreditCard(String creditCard) {
        if (creditCard == null || creditCard.isEmpty()) {
            return null;
        }
        if (CREDIT_CARD_PATTERN.matcher(creditCard).matches()) {
            if (creditCard.length() >= 13) {
                return creditCard.substring(0, 4) + "****" + creditCard.substring(creditCard.length() - 4);
            }
            return "****" + creditCard.substring(creditCard.length() - 4);
        }
        return mask(creditCard);
    }

    public String maskSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            return null;
        }
        if (SECRET_PATTERN.matcher(secret).matches()) {
            if (secret.length() >= 8) {
                return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
            }
        }
        return mask(secret);
    }

    public String mask(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value.length() <= 4) {
            return MASK_CHAR.repeat(value.length());
        }
        int maskLength = value.length() - VISIBLE_PREFIX - VISIBLE_SUFFIX;
        return value.substring(0, VISIBLE_PREFIX) + 
               MASK_CHAR.repeat(Math.min(maskLength, 8)) + 
               value.substring(value.length() - VISIBLE_SUFFIX);
    }

    public String encryptSensitive(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        try {
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            
            PBEKeySpec spec = new PBEKeySpec(
                    new String(encryptionKey.getEncoded(), StandardCharsets.UTF_8).toCharArray(),
                    iv,
                    65536,
                    256
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] key = factory.generateSecret(spec).getEncoded();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((plainText + Base64.getEncoder().encodeToString(key)).getBytes(StandardCharsets.UTF_8));
            
            byte[] encrypted = new byte[iv.length + hash.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(hash, 0, encrypted, iv.length, hash.length);
            
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return hashSha256(plainText);
        }
    }

    public String hashSha256(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private SecretKey deriveKey(String keyMaterial) {
        try {
            if (keyMaterial == null || keyMaterial.isEmpty()) {
                byte[] randomKey = new byte[32];
                secureRandom.nextBytes(randomKey);
                return new javax.crypto.spec.SecretKeySpec(randomKey, "AES");
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));
            return new javax.crypto.spec.SecretKeySpec(hash, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
