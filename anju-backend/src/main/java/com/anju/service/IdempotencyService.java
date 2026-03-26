package com.anju.service;

import com.anju.entity.IdempotencyEntry;
import com.anju.repository.IdempotencyEntryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    private static final Duration APPOINTMENT_TTL = Duration.ofDays(7);

    private final IdempotencyEntryRepository idempotencyEntryRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyEntryRepository idempotencyEntryRepository, ObjectMapper objectMapper) {
        this.idempotencyEntryRepository = idempotencyEntryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public boolean isDuplicate(String idempotencyKey, String operation) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }

        Optional<IdempotencyEntry> entryOpt = idempotencyEntryRepository
                .findByOperationAndIdempotencyKey(operation, idempotencyKey);

        if (entryOpt.isEmpty()) {
            return false;
        }

        IdempotencyEntry entry = entryOpt.get();
        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        log.info("Duplicate operation detected: operation={}, key={}", operation, idempotencyKey);
        return true;
    }

    @Transactional
    public void recordOperation(String idempotencyKey, String operation, Object result) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        Duration ttl = "APPOINTMENT".equals(operation) ? APPOINTMENT_TTL : DEFAULT_TTL;
        LocalDateTime expiresAt = LocalDateTime.now().plus(ttl);

        IdempotencyEntry entry = idempotencyEntryRepository
                .findByOperationAndIdempotencyKey(operation, idempotencyKey)
                .orElseGet(IdempotencyEntry::new);

        entry.setOperation(operation);
        entry.setIdempotencyKey(idempotencyKey);
        entry.setExpiresAt(expiresAt);
        entry.setResultType(result != null ? result.getClass().getName() : null);
        entry.setResultPayload(serializeResult(result));

        idempotencyEntryRepository.save(entry);

        log.debug("Recorded idempotency key: operation={}, key={}, ttl={}", operation, idempotencyKey, ttl);
    }

    @Transactional(readOnly = true)
    public Object getCachedResult(String idempotencyKey, String operation) {
        return getCachedResult(idempotencyKey, operation, Object.class);
    }

    @Transactional(readOnly = true)
    public <T> T getCachedResult(String idempotencyKey, String operation, Class<T> resultType) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }

        Optional<IdempotencyEntry> entryOpt = idempotencyEntryRepository
                .findByOperationAndIdempotencyKey(operation, idempotencyKey);
        if (entryOpt.isEmpty()) {
            return null;
        }

        IdempotencyEntry entry = entryOpt.get();
        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        if (entry.getResultPayload() == null) {
            return null;
        }

        try {
            return objectMapper.readValue(entry.getResultPayload(), resultType);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize idempotency payload for operation={}, key={}", operation, idempotencyKey, e);
            return null;
        }
    }

    @Transactional
    public void remove(String idempotencyKey, String operation) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        idempotencyEntryRepository.findByOperationAndIdempotencyKey(operation, idempotencyKey)
                .ifPresent(idempotencyEntryRepository::delete);
    }

    @Transactional
    public void cleanup() {
        int removed = idempotencyEntryRepository.deleteExpired(LocalDateTime.now());
        if (removed > 0) {
            log.debug("Cleaned up {} expired idempotency entries", removed);
        }
    }

    @Transactional(readOnly = true)
    public int getActiveCount() {
        return Math.toIntExact(idempotencyEntryRepository.countByExpiresAtAfter(LocalDateTime.now()));
    }

    private String serializeResult(Object result) {
        if (result == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize idempotency result, storing null payload", e);
            return null;
        }
    }
}
