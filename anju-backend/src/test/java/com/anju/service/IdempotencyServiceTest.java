package com.anju.service;

import com.anju.entity.IdempotencyEntry;
import com.anju.repository.IdempotencyEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyEntryRepository idempotencyEntryRepository;

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService(idempotencyEntryRepository, new ObjectMapper());
    }

    @Test
    @DisplayName("Should return false for new idempotency key")
    void shouldReturnFalseForNewKey() {
        when(idempotencyEntryRepository.findByOperationAndIdempotencyKey("PAYMENT", "unique-key-123"))
                .thenReturn(Optional.empty());

        assertFalse(idempotencyService.isDuplicate("unique-key-123", "PAYMENT"));
    }

    @Test
    @DisplayName("Should return true for duplicate operation")
    void shouldReturnTrueForDuplicate() {
        IdempotencyEntry entry = new IdempotencyEntry();
        entry.setOperation("PAYMENT");
        entry.setIdempotencyKey("unique-key-123");
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(idempotencyEntryRepository.findByOperationAndIdempotencyKey("PAYMENT", "unique-key-123"))
                .thenReturn(Optional.of(entry));

        assertTrue(idempotencyService.isDuplicate("unique-key-123", "PAYMENT"));
    }

    @Test
    @DisplayName("Should not flag expired operation as duplicate")
    void shouldNotFlagExpiredEntryAsDuplicate() {
        IdempotencyEntry entry = new IdempotencyEntry();
        entry.setOperation("PAYMENT");
        entry.setIdempotencyKey("expired-key");
        entry.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(idempotencyEntryRepository.findByOperationAndIdempotencyKey("PAYMENT", "expired-key"))
                .thenReturn(Optional.of(entry));

        assertFalse(idempotencyService.isDuplicate("expired-key", "PAYMENT"));
    }

    @Test
    @DisplayName("Should cache and retrieve typed result")
    void shouldCacheAndRetrieveTypedResult() {
        when(idempotencyEntryRepository.findByOperationAndIdempotencyKey(eq("PAYMENT"), eq("unique-key-456")))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(buildEntry("PAYMENT", "unique-key-456", "\"transaction-123\"")));
        when(idempotencyEntryRepository.save(any(IdempotencyEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        idempotencyService.recordOperation("unique-key-456", "PAYMENT", "transaction-123");

        String cachedResult = idempotencyService.getCachedResult("unique-key-456", "PAYMENT", String.class);
        assertEquals("transaction-123", cachedResult);
    }

    @Test
    @DisplayName("Should remove idempotency record")
    void shouldRemoveRecord() {
        IdempotencyEntry entry = buildEntry("PAYMENT", "unique-key-789", "\"result\"");
        when(idempotencyEntryRepository.findByOperationAndIdempotencyKey("PAYMENT", "unique-key-789"))
                .thenReturn(Optional.of(entry))
                .thenReturn(Optional.empty());

        idempotencyService.remove("unique-key-789", "PAYMENT");

        assertFalse(idempotencyService.isDuplicate("unique-key-789", "PAYMENT"));
    }

    @Test
    @DisplayName("Should handle null idempotency key gracefully")
    void shouldHandleNullKeyGracefully() {
        assertFalse(idempotencyService.isDuplicate(null, "PAYMENT"));
        idempotencyService.recordOperation(null, "PAYMENT", "result");
        assertNull(idempotencyService.getCachedResult(null, "PAYMENT", String.class));
    }

    @Test
    @DisplayName("Should track active count")
    void shouldTrackActiveCount() {
        when(idempotencyEntryRepository.countByExpiresAtAfter(any(LocalDateTime.class))).thenReturn(2L);
        assertEquals(2, idempotencyService.getActiveCount());
    }

    private IdempotencyEntry buildEntry(String operation, String key, String payload) {
        IdempotencyEntry entry = new IdempotencyEntry();
        entry.setOperation(operation);
        entry.setIdempotencyKey(key);
        entry.setResultPayload(payload);
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        return entry;
    }
}
