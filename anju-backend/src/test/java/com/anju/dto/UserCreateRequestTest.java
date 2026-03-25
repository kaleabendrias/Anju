package com.anju.dto;

import com.anju.entity.User;
import com.anju.entity.User.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @Test
        @DisplayName("Should reject password shorter than 8 characters")
        void shouldRejectShortPassword() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username("testuser")
                    .password("Ab1")
                    .role(Role.ADMIN)
                    .build();

            Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        }

        @Test
        @DisplayName("Should reject password without letters")
        void shouldRejectPasswordWithoutLetters() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username("testuser")
                    .password("12345678")
                    .role(Role.ADMIN)
                    .build();

            Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("letters")));
        }

        @Test
        @DisplayName("Should reject password without numbers")
        void shouldRejectPasswordWithoutNumbers() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username("testuser")
                    .password("Abcdefgh")
                    .role(Role.ADMIN)
                    .build();

            Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("numbers")));
        }

        @Test
        @DisplayName("Should accept valid password")
        void shouldAcceptValidPassword() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username("testuser")
                    .password("Password123")
                    .role(Role.ADMIN)
                    .build();

            Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Username Validation Tests")
    class UsernameValidationTests {

        @Test
        @DisplayName("Should reject null username")
        void shouldRejectNullUsername() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username(null)
                    .password("Password123")
                    .role(Role.ADMIN)
                    .build();

            Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        }

        @Test
        @DisplayName("Should reject blank username")
        void shouldRejectBlankUsername() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username("   ")
                    .password("Password123")
                    .role(Role.ADMIN)
                    .build();

            Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject username shorter than 3 characters")
        void shouldRejectShortUsername() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username("ab")
                    .password("Password123")
                    .role(Role.ADMIN)
                    .build();

            Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Role Validation Tests")
    class RoleValidationTests {

        @Test
        @DisplayName("Should reject null role")
        void shouldRejectNullRole() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username("testuser")
                    .password("Password123")
                    .role(null)
                    .build();

            Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("role")));
        }
    }

    @Nested
    @DisplayName("Entity Conversion Tests")
    class EntityConversionTests {

        @Test
        @DisplayName("Should convert to User entity correctly")
        void shouldConvertToUserEntityCorrectly() {
            UserCreateRequest request = UserCreateRequest.builder()
                    .username("testuser")
                    .password("Password123")
                    .role(Role.ADMIN)
                    .build();

            User user = UserCreateRequest.toEntity(request, "encodedPassword");

            assertEquals("testuser", user.getUsername());
            assertEquals("encodedPassword", user.getPasswordHash());
            assertEquals(Role.ADMIN, user.getRole());
        }

        @Test
        @DisplayName("Should convert all roles correctly")
        void shouldConvertAllRolesCorrectly() {
            for (Role role : Role.values()) {
                UserCreateRequest request = UserCreateRequest.builder()
                        .username("testuser")
                        .password("Password123")
                        .role(role)
                        .build();

                User user = UserCreateRequest.toEntity(request, "hash");

                assertEquals(role, user.getRole());
            }
        }
    }
}
