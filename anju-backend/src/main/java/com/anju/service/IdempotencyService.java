package com.anju.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    private static final Duration APPOINTMENT_TTL = Duration.ofDays(7);

    private final Map<String, IdempotencyRecord> records = new ConcurrentHashMap<>();

    public boolean isDuplicate(String idempotencyKey, String operation) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }

        String fullKey = operation + ":" + idempotencyKey;
        IdempotencyRecord record = records.get(fullKey);

        if (record == null) {
            return false;
        }

        if (record.isExpired()) {
            records.remove(fullKey);
            return false;
        }

        log.info("Duplicate operation detected: operation={}, key={}", operation, idempotencyKey);
        return true;
    }

    public void recordOperation(String idempotencyKey, String operation, Object result) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        Duration ttl = "APPOINTMENT".equals(operation) ? APPOINTMENT_TTL : DEFAULT_TTL;
        String fullKey = operation + ":" + idempotencyKey;
        records.put(fullKey, new IdempotencyRecord(result, Instant.now().plus(ttl)));
        
        log.debug("Recorded idempotency key: operation={}, key={}, ttl={}", operation, idempotencyKey, ttl);
    }

    public Object getCachedResult(String idempotencyKey, String operation) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }

        String fullKey = operation + ":" + idempotencyKey;
        IdempotencyRecord record = records.get(fullKey);

        if (record == null || record.isExpired()) {
            return null;
        }

        return record.result();
    }

    public void remove(String idempotencyKey, String operation) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        String fullKey = operation + ":" + idempotencyKey;
        records.remove(fullKey);
    }

    public void cleanup() {
        Instant now = Instant.now();
        records.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    public int getActiveCount() {
        cleanup();
        return records.size();
    }

    private record IdempotencyRecord(Object result, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
