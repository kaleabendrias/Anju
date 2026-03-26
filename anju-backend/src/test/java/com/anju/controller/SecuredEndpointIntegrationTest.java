package com.anju.controller;

import com.anju.dto.*;
import com.anju.entity.Appointment;
import com.anju.entity.User;
import com.anju.repository.*;
import com.anju.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecuredEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String frontlineToken;
    private String financeToken;
    private String dispatcherToken;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        appointmentRepository.deleteAll();
        propertyRepository.deleteAll();

        User adminUser = userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("admin")
                        .passwordHash("$2a$10$encoded")
                        .role(User.Role.ADMIN)
                        .build()));

        User frontlineUser = userRepository.findByUsername("frontline")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("frontline")
                        .passwordHash("$2a$10$encoded")
                        .role(User.Role.FRONTLINE)
                        .build()));

        User financeUser = userRepository.findByUsername("finance")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("finance")
                        .passwordHash("$2a$10$encoded")
                        .role(User.Role.FINANCE)
                        .build()));

        User dispatcherUser = userRepository.findByUsername("dispatcher")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("dispatcher")
                        .passwordHash("$2a$10$encoded")
                        .role(User.Role.DISPATCHER)
                        .build()));

        adminToken = jwtTokenProvider.generateToken(adminUser.getUsername(), "ADMIN");
        frontlineToken = jwtTokenProvider.generateToken(frontlineUser.getUsername(), "FRONTLINE");
        financeToken = jwtTokenProvider.generateToken(financeUser.getUsername(), "FINANCE");
        dispatcherToken = jwtTokenProvider.generateToken(dispatcherUser.getUsername(), "DISPATCHER");
    }

    @Nested
    @DisplayName("POST /api/admin/appointments - Create Appointment")
    class CreateAppointmentTests {

        @Test
        @Order(1)
        @DisplayName("Should create appointment with valid ADMIN token")
        void shouldCreateAppointmentWithAdminToken() throws Exception {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(com.anju.entity.Appointment.ServiceType.STANDARD_CONSULTATION)
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(30))
                    .patientName("Test Patient")
                    .orderAmount(new BigDecimal("100"))
                    .build();

            mockMvc.perform(post("/api/admin/appointments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @Order(2)
        @DisplayName("Should create appointment with valid FRONTLINE token")
        void shouldCreateAppointmentWithFrontlineToken() throws Exception {
            LocalDateTime startTime = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(com.anju.entity.Appointment.ServiceType.EXTENDED_CONSULTATION)
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(60))
                    .patientName("Another Patient")
                    .orderAmount(new BigDecimal("200"))
                    .build();

            mockMvc.perform(post("/api/admin/appointments")
                            .header("Authorization", "Bearer " + frontlineToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(3)
        @DisplayName("Should return 401 without token")
        void shouldReturn401WithoutToken() throws Exception {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(com.anju.entity.Appointment.ServiceType.QUICK_CONSULTATION)
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(15))
                    .patientName("Test Patient")
                    .orderAmount(new BigDecimal("100"))
                    .build();

            mockMvc.perform(post("/api/admin/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(4)
        @DisplayName("Should return 403 with wrong role (FINANCE)")
        void shouldReturn403WithWrongRole() throws Exception {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(com.anju.entity.Appointment.ServiceType.STANDARD_CONSULTATION)
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(30))
                    .patientName("Test Patient")
                    .orderAmount(new BigDecimal("100"))
                    .build();

            mockMvc.perform(post("/api/admin/appointments")
                            .header("Authorization", "Bearer " + financeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/appointments/{id}/cancel - Cancel Appointment")
    class CancelAppointmentTests {

        @Test
        @Order(1)
        @DisplayName("Should cancel appointment with valid ADMIN token")
        void shouldCancelAppointmentWithAdminToken() throws Exception {
            Long appointmentId = createTestAppointment(adminToken);

            mockMvc.perform(post("/api/appointments/" + appointmentId + "/cancel")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(2)
        @DisplayName("Should cancel appointment with FRONTLINE token")
        void shouldCancelAppointmentWithFrontlineToken() throws Exception {
            Long appointmentId = createTestAppointment(frontlineToken);

            mockMvc.perform(post("/api/appointments/" + appointmentId + "/cancel")
                            .header("Authorization", "Bearer " + frontlineToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(3)
        @DisplayName("Should return 401 without token")
        void shouldReturn401WithoutToken() throws Exception {
            mockMvc.perform(post("/api/appointments/1/cancel"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(4)
        @DisplayName("Should return 403 with wrong role (FINANCE)")
        void shouldReturn403WithWrongRole() throws Exception {
            Long appointmentId = createTestAppointment(adminToken);

            mockMvc.perform(post("/api/appointments/" + appointmentId + "/cancel")
                            .header("Authorization", "Bearer " + financeToken))
                    .andExpect(status().isForbidden());
        }

        private Long createTestAppointment(String token) throws Exception {
            LocalDateTime startTime = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(com.anju.entity.Appointment.ServiceType.STANDARD_CONSULTATION)
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(30))
                    .patientName("Cancel Test Patient")
                    .orderAmount(new BigDecimal("150"))
                    .build();

            MvcResult result = mockMvc.perform(post("/api/admin/appointments")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            ApiResponse<AppointmentResponse> response = objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AppointmentResponse.class));
            return response.getData().getId();
        }
    }

    @Nested
    @DisplayName("POST /api/finance/payments - Record Payment")
    class RecordPaymentTests {

        @Test
        @Order(1)
        @DisplayName("Should record payment with valid ADMIN token")
        void shouldRecordPaymentWithAdminToken() throws Exception {
            Long appointmentId = createTestAppointmentForPayment();

            PaymentRequest request = PaymentRequest.builder()
                    .idempotencyKey("payment_" + UUID.randomUUID())
                    .appointmentId(appointmentId)
                    .amount(new BigDecimal("100.00"))
                    .channel(com.anju.entity.Transaction.PaymentChannel.ALIPAY)
                    .build();

            mockMvc.perform(post("/api/finance/payments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @Order(2)
        @DisplayName("Should record payment with FINANCE token")
        void shouldRecordPaymentWithFinanceToken() throws Exception {
            Long appointmentId = createTestAppointmentForPayment();

            PaymentRequest request = PaymentRequest.builder()
                    .idempotencyKey("payment_" + UUID.randomUUID())
                    .appointmentId(appointmentId)
                    .amount(new BigDecimal("150.00"))
                    .channel(com.anju.entity.Transaction.PaymentChannel.WECHAT_MOCK)
                    .build();

            mockMvc.perform(post("/api/finance/payments")
                            .header("Authorization", "Bearer " + financeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(3)
        @DisplayName("Should record payment with FRONTLINE token")
        void shouldRecordPaymentWithFrontlineToken() throws Exception {
            Long appointmentId = createTestAppointmentForPayment();

            PaymentRequest request = PaymentRequest.builder()
                    .idempotencyKey("payment_" + UUID.randomUUID())
                    .appointmentId(appointmentId)
                    .amount(new BigDecimal("200.00"))
                    .channel(com.anju.entity.Transaction.PaymentChannel.ALIPAY)
                    .build();

            mockMvc.perform(post("/api/finance/payments")
                            .header("Authorization", "Bearer " + frontlineToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(4)
        @DisplayName("Should return 401 without token")
        void shouldReturn401WithoutToken() throws Exception {
            PaymentRequest request = PaymentRequest.builder()
                    .idempotencyKey("payment_" + UUID.randomUUID())
                    .appointmentId(1L)
                    .amount(new BigDecimal("100.00"))
                    .channel(com.anju.entity.Transaction.PaymentChannel.ALIPAY)
                    .build();

            mockMvc.perform(post("/api/finance/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(5)
        @DisplayName("Should return 403 with wrong role (DISPATCHER)")
        void shouldReturn403WithWrongRole() throws Exception {
            PaymentRequest request = PaymentRequest.builder()
                    .idempotencyKey("payment_" + UUID.randomUUID())
                    .appointmentId(1L)
                    .amount(new BigDecimal("100.00"))
                    .channel(com.anju.entity.Transaction.PaymentChannel.ALIPAY)
                    .build();

            mockMvc.perform(post("/api/finance/payments")
                            .header("Authorization", "Bearer " + dispatcherToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        private Long createTestAppointmentForPayment() throws Exception {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(com.anju.entity.Appointment.ServiceType.STANDARD_CONSULTATION)
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(30))
                    .patientName("Test Patient")
                    .orderAmount(new BigDecimal("100"))
                    .build();

            MvcResult result = mockMvc.perform(post("/api/admin/appointments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            ApiResponse<AppointmentResponse> response = objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AppointmentResponse.class));
            return response.getData().getId();
        }
    }

    @Nested
    @DisplayName("POST /api/finance/settlements/generate - Generate Settlement")
    class GenerateSettlementTests {

        @Test
        @Order(1)
        @DisplayName("Should generate settlement with ADMIN token")
        void shouldGenerateSettlementWithAdminToken() throws Exception {
            String date = LocalDate.now().minusDays(1).toString();

            mockMvc.perform(post("/api/finance/settlements/generate")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("date", date))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.settlementDate").value(date));
        }

        @Test
        @Order(2)
        @DisplayName("Should generate settlement with FINANCE token")
        void shouldGenerateSettlementWithFinanceToken() throws Exception {
            String date = LocalDate.now().minusDays(2).toString();

            mockMvc.perform(post("/api/finance/settlements/generate")
                            .header("Authorization", "Bearer " + financeToken)
                            .param("date", date))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(3)
        @DisplayName("Should return 401 without token")
        void shouldReturn401WithoutToken() throws Exception {
            mockMvc.perform(post("/api/finance/settlements/generate")
                            .param("date", LocalDate.now().minusDays(1).toString()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(4)
        @DisplayName("Should return 403 with wrong role (FRONTLINE)")
        void shouldReturn403WithWrongRole() throws Exception {
            mockMvc.perform(post("/api/finance/settlements/generate")
                            .header("Authorization", "Bearer " + frontlineToken)
                            .param("date", LocalDate.now().minusDays(1).toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(5)
        @DisplayName("Should return 403 with DISPATCHER role")
        void shouldReturn403WithDispatcherRole() throws Exception {
            mockMvc.perform(post("/api/finance/settlements/generate")
                            .header("Authorization", "Bearer " + dispatcherToken)
                            .param("date", LocalDate.now().minusDays(1).toString()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Appointment Data Isolation")
    class AppointmentIsolationTests {

        @Test
        @DisplayName("Should return only owner's appointments for status-filtered listing")
        void shouldReturnOnlyOwnerAppointmentsForStatusFilteredListing() throws Exception {
            createAppointment(adminToken, "Admin Patient", 1);
            createAppointment(frontlineToken, "Frontline Patient", 2);

            mockMvc.perform(get("/api/appointments")
                            .param("status", "PENDING")
                            .header("Authorization", "Bearer " + frontlineToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].patientName").value("Frontline Patient"));
        }
    }

    @Nested
    @DisplayName("Appointment CSV Import/Export")
    class AppointmentCsvImportExportTests {

        @Test
        @DisplayName("Should reject CSV import without authentication")
        void shouldRejectImportWithoutAuthentication() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "appointments.csv",
                    "text/csv",
                    "service_type,start_time,end_time,patient_name\nSTANDARD_CONSULTATION,2026-04-01 10:00:00,2026-04-01 10:30:00,Test User\n".getBytes());

            mockMvc.perform(multipart("/api/admin/appointments/import").file(file))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should enforce idempotency for CSV import")
        void shouldEnforceIdempotencyForCsvImport() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "appointments.csv",
                    "text/csv",
                    "service_type,start_time,end_time,patient_name\nSTANDARD_CONSULTATION,2026-04-01 10:00:00,2026-04-01 10:30:00,CSV User\n".getBytes());

            String key = "csv-import-key-001";

            mockMvc.perform(multipart("/api/admin/appointments/import")
                            .file(file)
                            .header("Authorization", "Bearer " + adminToken)
                            .header("Idempotency-Key", key))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fromCache").value(false));

            mockMvc.perform(multipart("/api/admin/appointments/import")
                            .file(file)
                            .header("Authorization", "Bearer " + adminToken)
                            .header("Idempotency-Key", key))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fromCache").value(true));
        }
    }

    private Long createAppointment(String token, String patientName, int dayOffset) throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusDays(dayOffset).withHour(9).withMinute(0);
        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .serviceType(com.anju.entity.Appointment.ServiceType.STANDARD_CONSULTATION)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .patientName(patientName)
                .orderAmount(new BigDecimal("100"))
                .build();

        MvcResult result = mockMvc.perform(post("/api/admin/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ApiResponse<AppointmentResponse> response = objectMapper.readValue(content,
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AppointmentResponse.class));
        return response.getData().getId();
    }
}
