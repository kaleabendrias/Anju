package com.anju.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    @DisplayName("Should create success response with data")
    void shouldCreateSuccessResponseWithData() {
        ApiResponse<String> response = ApiResponse.success("Test Data");

        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals("Test Data", response.getData());
    }

    @Test
    @DisplayName("Should create success response with custom message")
    void shouldCreateSuccessResponseWithCustomMessage() {
        ApiResponse<String> response = ApiResponse.success("Custom Message", "Data");

        assertTrue(response.isSuccess());
        assertEquals("Custom Message", response.getMessage());
        assertEquals("Data", response.getData());
    }

    @Test
    @DisplayName("Should create error response")
    void shouldCreateErrorResponse() {
        ApiResponse<String> response = ApiResponse.error("Error occurred");

        assertFalse(response.isSuccess());
        assertEquals("Error occurred", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should handle null data in success response")
    void shouldHandleNullDataInSuccessResponse() {
        ApiResponse<Void> response = ApiResponse.success("Success", null);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should support generic types")
    void shouldSupportGenericTypes() {
        ApiResponse<Integer> intResponse = ApiResponse.success(42);
        ApiResponse<UserResponse> userResponse = ApiResponse.success(new UserResponse());

        assertEquals(42, intResponse.getData());
        assertNotNull(userResponse.getData());
    }

    @Test
    @DisplayName("Should preserve data type in builder")
    void shouldPreserveDataTypeInBuilder() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Test")
                .data("TestData")
                .build();

        assertEquals("TestData", response.getData());
        assertEquals(String.class, response.getData().getClass());
    }
}
