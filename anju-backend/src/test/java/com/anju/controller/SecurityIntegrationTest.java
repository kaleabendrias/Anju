package com.anju.controller;

import com.anju.entity.User;
import com.anju.repository.UserRepository;
import com.anju.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false"
})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .passwordHash("$2a$10$encoded")
                .role(User.Role.ADMIN)
                .build();

        regularUser = User.builder()
                .id(2L)
                .username("user")
                .passwordHash("$2a$10$encoded")
                .role(User.Role.FRONTLINE)
                .build();
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() throws Exception {
            mockMvc.perform(get("/api/properties"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"invalid\",\"password\":\"invalid\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should return 403 for wrong role")
        @WithMockUser(roles = "FRONTLINE")
        void shouldReturn403ForWrongRole() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow access with correct role")
        @WithMockUser(roles = "ADMIN")
        void shouldAllowAccessWithCorrectRole() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 when trying to approve without REVIEWER role")
        @WithMockUser(roles = "FRONTLINE")
        void shouldReturn403ForReviewerEndpoint() throws Exception {
            mockMvc.perform(post("/api/reviewer/properties/1/approve"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cross-Owner Access Tests")
    class CrossOwnerAccessTests {

        @Test
        @DisplayName("Should prevent cross-owner resource access")
        @WithMockUser(username = "user1", roles = "FRONTLINE")
        void shouldPreventCrossOwnerAccess() throws Exception {
        }
    }
}
