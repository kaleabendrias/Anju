package com.anju.security;

import com.anju.entity.User;
import com.anju.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class SecondaryVerificationServiceTest {

    @Autowired
    private SecondaryVerificationService secondaryVerificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .passwordHash(passwordEncoder.encode("TestPass123"))
                .role(User.Role.ADMIN)
                .build();
    }

    @Nested
    @DisplayName("Secondary Password Verification Tests")
    class SecondaryPasswordVerificationTests {

        @Test
        @DisplayName("Should verify correct secondary password")
        void shouldVerifyCorrectSecondaryPassword() {
            when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(testUser));

            boolean result = secondaryVerificationService.verifySecondaryPassword("testuser", "TestPass123");

            assertTrue(result);
        }

        @Test
        @DisplayName("Should reject incorrect secondary password")
        void shouldRejectIncorrectSecondaryPassword() {
            when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(testUser));

            boolean result = secondaryVerificationService.verifySecondaryPassword("testuser", "WrongPass123");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should reject null username")
        void shouldRejectNullUsername() {
            boolean result = secondaryVerificationService.verifySecondaryPassword(null, "TestPass123");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should reject empty password")
        void shouldRejectEmptyPassword() {
            when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(testUser));

            boolean result = secondaryVerificationService.verifySecondaryPassword("testuser", "");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should throw exception for non-existent user")
        void shouldThrowExceptionForNonExistentUser() {
            when(userRepository.findByUsername("nonexistent")).thenReturn(java.util.Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> 
                secondaryVerificationService.verifySecondaryPassword("nonexistent", "TestPass123")
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate with correct password")
        void shouldValidateWithCorrectPassword() {
            when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(testUser));

            assertDoesNotThrow(() -> 
                secondaryVerificationService.validateSecondaryPassword(testUser, "TestPass123")
            );
        }

        @Test
        @DisplayName("Should throw exception for invalid password")
        void shouldThrowExceptionForInvalidPassword() {
            when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(testUser));

            assertThrows(
                SecondaryVerificationService.SecondaryVerificationException.class,
                () -> secondaryVerificationService.validateSecondaryPassword(testUser, "WrongPass")
            );
        }
    }
}
