package com.anju.exception;

import com.anju.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
    }

    @Nested
    @DisplayName("HTTP Status Code Tests")
    class HttpStatusCodeTests {

        @Test
        @DisplayName("Should return 401 for UnauthorizedException")
        void shouldReturn401ForUnauthorized() {
            ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorizedException(
                    new UnauthorizedException("Invalid token"));
            
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 403 for ForbiddenException")
        void shouldReturn403ForForbidden() {
            ResponseEntity<ApiResponse<Void>> response = handler.handleForbiddenException(
                    new ForbiddenException("Access denied"));
            
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 403 for AccessDeniedException")
        void shouldReturn403ForAccessDenied() {
            ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDeniedException(
                    new AccessDeniedException("Insufficient permissions"));
            
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 404 for ResourceNotFoundException")
        void shouldReturn404ForNotFound() {
            ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFoundException(
                    new ResourceNotFoundException("Resource not found"));
            
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 409 for ConflictException")
        void shouldReturn409ForConflict() {
            ResponseEntity<ApiResponse<Void>> response = handler.handleConflictException(
                    new ConflictException("Resource conflict"));
            
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 400 for BusinessException")
        void shouldReturn400ForBusinessException() {
            ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(
                    new BusinessException("Business rule violation"));
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Error Response Tests")
    class ErrorResponseTests {

        @Test
        @DisplayName("Should return error message for UnauthorizedException")
        void shouldReturnErrorMessageForUnauthorized() {
            String message = "Invalid token";
            ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorizedException(
                    new UnauthorizedException(message));
            
            assertFalse(response.getBody().isSuccess());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should return error message for ForbiddenException")
        void shouldReturnErrorMessageForForbidden() {
            String message = "Access denied";
            ResponseEntity<ApiResponse<Void>> response = handler.handleForbiddenException(
                    new ForbiddenException(message));
            
            assertFalse(response.getBody().isSuccess());
            assertEquals(message, response.getBody().getMessage());
        }
    }
}
