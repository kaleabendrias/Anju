package com.anju.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        if (!EncryptionInitializer.isProperlyConfigured()) {
            return attribute;
        }
        return EncryptionInitializer.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (!EncryptionInitializer.isProperlyConfigured()) {
            return dbData;
        }
        return EncryptionInitializer.decrypt(dbData);
    }
}
