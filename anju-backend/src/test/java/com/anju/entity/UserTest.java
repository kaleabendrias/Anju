package com.anju.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Nested
    @DisplayName("UserDetails Implementation Tests")
    class UserDetailsTests {

        @Test
        @DisplayName("Should return correct authorities for ADMIN role")
        void shouldReturnCorrectAuthoritiesForAdminRole() {
            User user = User.builder()
                    .id(1L)
                    .username("admin")
                    .passwordHash("hash")
                    .role(User.Role.ADMIN)
                    .build();

            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            assertEquals(1, authorities.size());
            assertTrue(authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("Should return correct authorities for all roles")
        void shouldReturnCorrectAuthoritiesForAllRoles() {
            for (User.Role role : User.Role.values()) {
                User user = User.builder()
                        .id(1L)
                        .username("user")
                        .passwordHash("hash")
                        .role(role)
                        .build();

                Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

                assertEquals(1, authorities.size());
                assertTrue(authorities.stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name())));
            }
        }

        @Test
        @DisplayName("Should return username as principal")
        void shouldReturnUsernameAsPrincipal() {
            User user = User.builder()
                    .username("testuser")
                    .passwordHash("hash")
                    .role(User.Role.ADMIN)
                    .build();

            assertEquals("testuser", user.getUsername());
        }

        @Test
        @DisplayName("Should return password hash as password")
        void shouldReturnPasswordHashAsPassword() {
            User user = User.builder()
                    .username("testuser")
                    .passwordHash("hashedPassword123")
                    .role(User.Role.ADMIN)
                    .build();

            assertEquals("hashedPassword123", user.getPassword());
        }

        @Test
        @DisplayName("Account should not be expired, locked, or disabled")
        void accountShouldNotBeExpiredLockedOrDisabled() {
            User user = User.builder()
                    .username("testuser")
                    .passwordHash("hash")
                    .role(User.Role.ADMIN)
                    .build();

            assertTrue(user.isAccountNonExpired());
            assertTrue(user.isAccountNonLocked());
            assertTrue(user.isCredentialsNonExpired());
            assertTrue(user.isEnabled());
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build user with all fields")
        void shouldBuildUserWithAllFields() {
            LocalDateTime now = LocalDateTime.now();

            User user = User.builder()
                    .id(1L)
                    .username("testuser")
                    .passwordHash("hash")
                    .role(User.Role.REVIEWER)
                    .createdAt(now)
                    .build();

            assertEquals(1L, user.getId());
            assertEquals("testuser", user.getUsername());
            assertEquals("hash", user.getPasswordHash());
            assertEquals(User.Role.REVIEWER, user.getRole());
            assertEquals(now, user.getCreatedAt());
        }

        @Test
        @DisplayName("Should use default role values")
        void shouldUseDefaultRoleValues() {
            User user = User.builder()
                    .username("testuser")
                    .passwordHash("hash")
                    .role(User.Role.FRONTLINE)
                    .build();

            assertEquals(User.Role.FRONTLINE, user.getRole());
        }
    }

    @Nested
    @DisplayName("Role Enum Tests")
    class RoleEnumTests {

        @Test
        @DisplayName("Should have all expected roles")
        void shouldHaveAllExpectedRoles() {
            assertEquals(5, User.Role.values().length);
        }

        @Test
        @DisplayName("Should have ADMIN role")
        void shouldHaveAdminRole() {
            assertNotNull(User.Role.ADMIN);
            assertEquals("ADMIN", User.Role.ADMIN.name());
        }

        @Test
        @DisplayName("Should have REVIEWER role")
        void shouldHaveReviewerRole() {
            assertNotNull(User.Role.REVIEWER);
            assertEquals("REVIEWER", User.Role.REVIEWER.name());
        }

        @Test
        @DisplayName("Should have DISPATCHER role")
        void shouldHaveDispatcherRole() {
            assertNotNull(User.Role.DISPATCHER);
            assertEquals("DISPATCHER", User.Role.DISPATCHER.name());
        }

        @Test
        @DisplayName("Should have FINANCE role")
        void shouldHaveFinanceRole() {
            assertNotNull(User.Role.FINANCE);
            assertEquals("FINANCE", User.Role.FINANCE.name());
        }

        @Test
        @DisplayName("Should have FRONTLINE role")
        void shouldHaveFrontlineRole() {
            assertNotNull(User.Role.FRONTLINE);
            assertEquals("FRONTLINE", User.Role.FRONTLINE.name());
        }
    }
}
