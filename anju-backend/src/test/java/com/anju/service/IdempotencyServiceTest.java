package com.anju.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService();
    }

    @Test
    @DisplayName("Should return false for new idempotency key")
    void shouldReturnFalseForNewKey() {
        assertFalse(idempotencyService.isDuplicate("unique-key-123", "PAYMENT"));
    }

    @Test
    @DisplayName("Should return true for duplicate operation")
    void shouldReturnTrueForDuplicate() {
        idempotencyService.recordOperation("unique-key-123", "PAYMENT", "result");
        
        assertTrue(idempotencyService.isDuplicate("unique-key-123", "PAYMENT"));
    }

    @Test
    @DisplayName("Should not flag different operations as duplicates")
    void shouldNotFlagDifferentOperations() {
        idempotencyService.recordOperation("unique-key-123", "PAYMENT", "result");
        
        assertFalse(idempotencyService.isDuplicate("unique-key-123", "REFUND"));
    }

    @Test
    @DisplayName("Should cache and retrieve result")
    void shouldCacheAndRetrieveResult() {
        String expectedResult = "transaction-123";
        idempotencyService.recordOperation("unique-key-456", "PAYMENT", expectedResult);
        
        Object cachedResult = idempotencyService.getCachedResult("unique-key-456", "PAYMENT");
        
        assertEquals(expectedResult, cachedResult);
    }

    @Test
    @DisplayName("Should remove idempotency record")
    void shouldRemoveRecord() {
        idempotencyService.recordOperation("unique-key-789", "PAYMENT", "result");
        assertTrue(idempotencyService.isDuplicate("unique-key-789", "PAYMENT"));
        
        idempotencyService.remove("unique-key-789", "PAYMENT");
        
        assertFalse(idempotencyService.isDuplicate("unique-key-789", "PAYMENT"));
    }

    @Test
    @DisplayName("Should handle null idempotency key gracefully")
    void shouldHandleNullKeyGracefully() {
        assertFalse(idempotencyService.isDuplicate(null, "PAYMENT"));
        idempotencyService.recordOperation(null, "PAYMENT", "result");
        assertFalse(idempotencyService.isDuplicate(null, "PAYMENT"));
    }

    @Test
    @DisplayName("Should handle blank idempotency key gracefully")
    void shouldHandleBlankKeyGracefully() {
        assertFalse(idempotencyService.isDuplicate("  ", "PAYMENT"));
        idempotencyService.recordOperation("  ", "PAYMENT", "result");
        assertFalse(idempotencyService.isDuplicate("  ", "PAYMENT"));
    }

    @Test
    @DisplayName("Should track active count")
    void shouldTrackActiveCount() {
        assertEquals(0, idempotencyService.getActiveCount());
        
        idempotencyService.recordOperation("key1", "PAYMENT", "result1");
        idempotencyService.recordOperation("key2", "REFUND", "result2");
        
        assertEquals(2, idempotencyService.getActiveCount());
    }
}
