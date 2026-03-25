package com.anju.controller;

import com.anju.dto.*;
import com.anju.entity.Appointment;
import com.anju.entity.Appointment.AppointmentStatus;
import com.anju.entity.Property;
import com.anju.entity.User;
import com.anju.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Integration tests require proper JWT authentication setup")
class ApiStatusCodeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    private User adminUser;
    private User frontlineUser;

    @BeforeEach
    void setUp() {
        propertyRepository.deleteAll();
        appointmentRepository.deleteAll();
        transactionRepository.deleteAll();
        settlementRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = userRepository.save(User.builder()
                .username("admin_" + System.currentTimeMillis())
                .passwordHash("$2a$10$encoded")
                .role(User.Role.ADMIN)
                .build());

        frontlineUser = userRepository.save(User.builder()
                .username("frontline_" + System.currentTimeMillis())
                .passwordHash("$2a$10$encoded")
                .role(User.Role.FRONTLINE)
                .build());
    }

    @Nested
    @DisplayName("401 Unauthorized Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UnauthorizedTests {

        @Test
        @Order(1)
        @DisplayName("GET /api/properties - should return 401 without token")
        void getPropertiesWithoutToken_returns401() throws Exception {
            mockMvc.perform(get("/api/properties"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(2)
        @DisplayName("GET /api/appointments - should return 401 without token")
        void getAppointmentsWithoutToken_returns401() throws Exception {
            mockMvc.perform(get("/api/appointments"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(3)
        @DisplayName("GET /api/finance/transactions - should return 401 without token")
        void getTransactionsWithoutToken_returns401() throws Exception {
            mockMvc.perform(get("/api/finance/transactions")
                            .param("startDate", "2024-01-01")
                            .param("endDate", "2024-12-31"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(4)
        @DisplayName("GET /api/files - should return 401 without token")
        void getFilesWithoutToken_returns401() throws Exception {
            mockMvc.perform(get("/api/files/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(5)
        @DisplayName("POST /api/admin/properties - should return 401 without token")
        void createPropertyWithoutToken_returns401() throws Exception {
            PropertyCreateRequest request = PropertyCreateRequest.builder()
                    .uniqueCode("TEST001")
                    .rent(new BigDecimal("1000"))
                    .deposit(new BigDecimal("2000"))
                    .build();

            mockMvc.perform(post("/api/admin/properties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(6)
        @DisplayName("POST /api/auth/login - should return 401 for invalid credentials")
        void loginWithInvalidCredentials_returns401() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("nonexistent")
                    .password("wrongpassword")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("403 Forbidden Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ForbiddenTests {

        @Test
        @Order(1)
        @DisplayName("FRONTLINE accessing admin endpoint should return 403")
        @WithMockUser(roles = "FRONTLINE")
        void frontlineAccessingAdmin_returns403() throws Exception {
            PropertyCreateRequest request = PropertyCreateRequest.builder()
                    .uniqueCode("TEST002")
                    .rent(new BigDecimal("1000"))
                    .deposit(new BigDecimal("2000"))
                    .build();

            mockMvc.perform(post("/api/admin/properties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(2)
        @DisplayName("Should return 403 for permanent delete without admin role")
        @WithMockUser(roles = "FRONTLINE")
        void permanentDeleteWithoutAdmin_returns403() throws Exception {
            mockMvc.perform(delete("/api/files/1/permanent"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(3)
        @DisplayName("GET /api/admin/appointments - FRONTLINE should return 403")
        @WithMockUser(roles = "FRONTLINE")
        void frontlineAccessingAdminAppointments_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/appointments"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("404 Not Found Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class NotFoundTests {

        @Test
        @Order(1)
        @DisplayName("GET /api/properties/{id} - should return 404 for non-existent property")
        @WithMockUser(roles = "ADMIN")
        void getNonExistentProperty_returns404() throws Exception {
            mockMvc.perform(get("/api/properties/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Order(2)
        @DisplayName("GET /api/appointments/{id} - should return 404 for non-existent appointment")
        @WithMockUser(roles = "ADMIN")
        void getNonExistentAppointment_returns404() throws Exception {
            mockMvc.perform(get("/api/appointments/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Order(3)
        @DisplayName("GET /api/finance/transactions/{id} - should return 404 for non-existent transaction")
        @WithMockUser(roles = "FINANCE")
        void getNonExistentTransaction_returns404() throws Exception {
            mockMvc.perform(get("/api/finance/transactions/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Order(4)
        @DisplayName("GET /api/files/{id} - should return 404 for non-existent file")
        @WithMockUser(roles = "ADMIN")
        void getNonExistentFile_returns404() throws Exception {
            mockMvc.perform(get("/api/files/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("409 Conflict Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ConflictTests {

        @Test
        @Order(1)
        @DisplayName("Creating property with duplicate unique code should return 409")
        @WithMockUser(roles = "ADMIN")
        void duplicateUniqueCode_returns409() throws Exception {
            Property existing = propertyRepository.save(Property.builder()
                    .uniqueCode("DUPLICATE001")
                    .status(Property.PropertyStatus.DRAFT)
                    .rent(new BigDecimal("1000"))
                    .deposit(new BigDecimal("2000"))
                    .build());

            PropertyCreateRequest request = PropertyCreateRequest.builder()
                    .uniqueCode("DUPLICATE001")
                    .rent(new BigDecimal("1000"))
                    .deposit(new BigDecimal("2000"))
                    .build();

            mockMvc.perform(post("/api/admin/properties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("400 Bad Request Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BadRequestTests {

        @Test
        @Order(1)
        @DisplayName("Creating property with missing required fields should return 400")
        @WithMockUser(roles = "ADMIN")
        void createPropertyWithMissingFields_returns400() throws Exception {
            PropertyCreateRequest request = PropertyCreateRequest.builder()
                    .uniqueCode("")
                    .build();

            mockMvc.perform(post("/api/admin/properties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(2)
        @DisplayName("Creating appointment with past start time should return 400")
        @WithMockUser(roles = "ADMIN")
        void createAppointmentWithPastTime_returns400() throws Exception {
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .startTime(LocalDateTime.now().minusDays(1))
                    .endTime(LocalDateTime.now().minusDays(1).plusHours(1))
                    .patientName("Test Patient")
                    .orderAmount(new BigDecimal("100"))
                    .build();

            mockMvc.perform(post("/api/admin/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(3)
        @DisplayName("Creating appointment with end time before start time should return 400")
        @WithMockUser(roles = "ADMIN")
        void createAppointmentWithInvalidTimeRange_returns400() throws Exception {
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .startTime(LocalDateTime.now().plusDays(2))
                    .endTime(LocalDateTime.now().plusDays(1))
                    .patientName("Test Patient")
                    .orderAmount(new BigDecimal("100"))
                    .build();

            mockMvc.perform(post("/api/admin/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
