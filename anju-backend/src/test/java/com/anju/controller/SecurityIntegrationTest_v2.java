package com.anju.controller;

import com.anju.config.SecurityConfig;
import com.anju.dto.*;
import com.anju.entity.*;
import com.anju.repository.*;
import com.anju.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecurityIntegrationTest_v2 {

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
    private FileRecordRepository fileRecordRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User adminUser;
    private User frontlineUser;
    private User financeUser;
    private User dispatcherUser;
    private User otherFrontlineUser;

    private String adminToken;
    private String frontlineToken;
    private String financeToken;
    private String dispatcherToken;
    private String otherFrontlineToken;

    @BeforeEach
    void setUp() {
        fileRecordRepository.deleteAll();
        transactionRepository.deleteAll();
        appointmentRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = userRepository.save(User.builder()
                .username("admin_sec_" + System.currentTimeMillis())
                .passwordHash("$2a$10$encoded")
                .role(User.Role.ADMIN)
                .build());

        frontlineUser = userRepository.save(User.builder()
                .username("frontline_sec_" + System.currentTimeMillis())
                .passwordHash("$2a$10$encoded")
                .role(User.Role.FRONTLINE)
                .build());

        financeUser = userRepository.save(User.builder()
                .username("finance_sec_" + System.currentTimeMillis())
                .passwordHash("$2a$10$encoded")
                .role(User.Role.FINANCE)
                .build());

        dispatcherUser = userRepository.save(User.builder()
                .username("dispatcher_sec_" + System.currentTimeMillis())
                .passwordHash("$2a$10$encoded")
                .role(User.Role.DISPATCHER)
                .build());

        otherFrontlineUser = userRepository.save(User.builder()
                .username("other_frontline_" + System.currentTimeMillis())
                .passwordHash("$2a$10$encoded")
                .role(User.Role.FRONTLINE)
                .build());

        adminToken = jwtTokenProvider.generateToken(adminUser.getUsername(), "ADMIN");
        frontlineToken = jwtTokenProvider.generateToken(frontlineUser.getUsername(), "FRONTLINE");
        financeToken = jwtTokenProvider.generateToken(financeUser.getUsername(), "FINANCE");
        dispatcherToken = jwtTokenProvider.generateToken(dispatcherUser.getUsername(), "DISPATCHER");
        otherFrontlineToken = jwtTokenProvider.generateToken(otherFrontlineUser.getUsername(), "FRONTLINE");
    }

    @Nested
    @DisplayName("JWT Authentication Filter Tests")
    class JwtAuthenticationFilterTests {

        @Test
        @Order(1)
        @DisplayName("Should authenticate valid JWT token")
        void validJwtToken_authenticated() throws Exception {
            mockMvc.perform(get("/api/properties")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(2)
        @DisplayName("Should reject expired JWT token")
        void expiredJwtToken_rejected() throws Exception {
            String expiredSecret = "TestSecretKeyForJwtTokenGeneration2024VerySecureKey256BitsForTestingPurposes1234567890";
            SecretKey key = Keys.hmacShaKeyFor(expiredSecret.getBytes(StandardCharsets.UTF_8));

            String expiredToken = Jwts.builder()
                    .subject("admin")
                    .claim("role", "ADMIN")
                    .issuedAt(new Date(System.currentTimeMillis() - 100000))
                    .expiration(new Date(System.currentTimeMillis() - 50000))
                    .signWith(key)
                    .compact();

            mockMvc.perform(get("/api/properties")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(3)
        @DisplayName("Should reject token with wrong signature")
        void wrongSignatureToken_rejected() throws Exception {
            String wrongSecret = "WrongSecretKeyForJwtTokenGeneration2024VerySecureKey256BitsForTesting1234567890";
            SecretKey key = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));

            String wrongSigToken = Jwts.builder()
                    .subject("admin")
                    .claim("role", "ADMIN")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(key)
                    .compact();

            mockMvc.perform(get("/api/properties")
                            .header("Authorization", "Bearer " + wrongSigToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(4)
        @DisplayName("Should reject malformed JWT token")
        void malformedJwtToken_rejected() throws Exception {
            mockMvc.perform(get("/api/properties")
                            .header("Authorization", "Bearer not.a.valid.jwt.token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(5)
        @DisplayName("Should accept token with role claim")
        void tokenWithRoleClaim_accepted() throws Exception {
            String tokenWithRole = jwtTokenProvider.generateToken(frontlineUser.getUsername(), "FRONTLINE");
            mockMvc.perform(get("/api/admin/appointments")
                            .header("Authorization", "Bearer " + tokenWithRole))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(6)
        @DisplayName("Should reject token without Bearer prefix")
        void tokenWithoutBearerPrefix_rejected() throws Exception {
            mockMvc.perform(get("/api/properties")
                            .header("Authorization", adminToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("RBAC Authorization Tests")
    class RbacAuthorizationTests {

        @Test
        @Order(1)
        @DisplayName("ADMIN should access all role-restricted endpoints")
        void adminAccessesAllRoleEndpoints() throws Exception {
            mockMvc.perform(get("/api/properties")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/finance/transactions")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("startDate", "2024-01-01")
                            .param("endDate", "2024-12-31"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/admin/properties")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"uniqueCode\":\"RBAC001\",\"rent\":1000,\"deposit\":2000}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(2)
        @DisplayName("FRONTLINE should access frontline endpoints but not finance")
        void frontlineAccessesOwnRoleEndpoints() throws Exception {
            mockMvc.perform(get("/api/properties")
                            .header("Authorization", "Bearer " + frontlineToken))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/finance/transactions")
                            .header("Authorization", "Bearer " + frontlineToken)
                            .param("startDate", "2024-01-01")
                            .param("endDate", "2024-12-31"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(3)
        @DisplayName("FINANCE should access finance endpoints but not admin operations")
        void financeAccessesOwnRoleEndpoints() throws Exception {
            mockMvc.perform(get("/api/finance/transactions")
                            .header("Authorization", "Bearer " + financeToken)
                            .param("startDate", "2024-01-01")
                            .param("endDate", "2024-12-31"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/admin/properties")
                            .header("Authorization", "Bearer " + financeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"uniqueCode\":\"RBAC002\",\"rent\":1000,\"deposit\":2000}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(4)
        @DisplayName("DISPATCHER should confirm appointments")
        void dispatcherConfirmsAppointments() throws Exception {
            Appointment appointment = appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                    .orderAmount(new BigDecimal("100"))
                    .operatorId(frontlineUser.getId())
                    .build());

            mockMvc.perform(post("/api/appointments/" + appointment.getId() + "/confirm")
                            .header("Authorization", "Bearer " + dispatcherToken))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(5)
        @DisplayName("FRONTLINE should NOT confirm appointments")
        void frontlineCannotConfirmAppointments() throws Exception {
            Appointment appointment = appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                    .orderAmount(new BigDecimal("100"))
                    .operatorId(frontlineUser.getId())
                    .build());

            mockMvc.perform(post("/api/appointments/" + appointment.getId() + "/confirm")
                            .header("Authorization", "Bearer " + frontlineToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Object-Level Authorization (IDOR) Tests")
    class ObjectLevelAuthorizationTests {

        @Test
        @Order(1)
        @DisplayName("User should NOT view another user's appointment")
        void userCannotViewOthersAppointment() throws Exception {
            Appointment othersAppointment = appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                    .orderAmount(new BigDecimal("100"))
                    .operatorId(frontlineUser.getId())
                    .build());

            mockMvc.perform(get("/api/appointments/" + othersAppointment.getId())
                            .header("Authorization", "Bearer " + otherFrontlineToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(2)
        @DisplayName("User should NOT cancel another user's appointment")
        void userCannotCancelOthersAppointment() throws Exception {
            Appointment othersAppointment = appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                    .orderAmount(new BigDecimal("100"))
                    .operatorId(frontlineUser.getId())
                    .build());

            mockMvc.perform(post("/api/appointments/" + othersAppointment.getId() + "/cancel")
                            .header("Authorization", "Bearer " + otherFrontlineToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(3)
        @DisplayName("User should view their own appointment")
        void userCanViewOwnAppointment() throws Exception {
            Appointment ownAppointment = appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                    .orderAmount(new BigDecimal("100"))
                    .operatorId(frontlineUser.getId())
                    .build());

            mockMvc.perform(get("/api/appointments/" + ownAppointment.getId())
                            .header("Authorization", "Bearer " + frontlineToken))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(4)
        @DisplayName("User should cancel their own appointment")
        void userCanCancelOwnAppointment() throws Exception {
            Appointment ownAppointment = appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                    .orderAmount(new BigDecimal("100"))
                    .operatorId(frontlineUser.getId())
                    .build());

            mockMvc.perform(post("/api/appointments/" + ownAppointment.getId() + "/cancel")
                            .header("Authorization", "Bearer " + frontlineToken))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(5)
        @DisplayName("ADMIN should view any appointment")
        void adminCanViewAnyAppointment() throws Exception {
            Appointment appointment = appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                    .orderAmount(new BigDecimal("100"))
                    .operatorId(frontlineUser.getId())
                    .build());

            mockMvc.perform(get("/api/appointments/" + appointment.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(6)
        @DisplayName("ADMIN should cancel any appointment")
        void adminCanCancelAnyAppointment() throws Exception {
            Appointment appointment = appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                    .orderAmount(new BigDecimal("100"))
                    .operatorId(frontlineUser.getId())
                    .build());

            mockMvc.perform(post("/api/appointments/" + appointment.getId() + "/cancel")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(7)
        @DisplayName("User should NOT access another user's file")
        void userCannotAccessOthersFile() throws Exception {
            FileRecord othersFile = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("LOGICAL001")
                    .fileHash("hash123")
                    .originalName("test.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/test.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            mockMvc.perform(get("/api/files/" + othersFile.getId())
                            .header("Authorization", "Bearer " + otherFrontlineToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(8)
        @DisplayName("User should access their own file")
        void userCanAccessOwnFile() throws Exception {
            FileRecord ownFile = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("LOGICAL002")
                    .fileHash("hash456")
                    .originalName("myfile.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/myfile.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            mockMvc.perform(get("/api/files/" + ownFile.getId())
                            .header("Authorization", "Bearer " + frontlineToken))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Secondary Password Verification Tests")
    class SecondaryPasswordVerificationTests {

        @Test
        @Order(1)
        @DisplayName("Refund without secondary password should fail")
        void refundWithoutSecondaryPassword_fails() throws Exception {
            Transaction original = transactionRepository.save(Transaction.builder()
                    .trxId("TRX_REFUND_TEST")
                    .amount(new BigDecimal("100"))
                    .type(Transaction.TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .refundableFlag(true)
                    .build());

            RefundRequest request = RefundRequest.builder()
                    .originalTransactionId(original.getId())
                    .idempotencyKey("refund_" + System.currentTimeMillis())
                    .reason("Test refund")
                    .build();

            mockMvc.perform(post("/api/finance/refunds")
                            .header("Authorization", "Bearer " + financeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(2)
        @DisplayName("Permanent file delete without secondary password should fail")
        void permanentDeleteWithoutSecondaryPassword_fails() throws Exception {
            FileRecord deletedFile = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("LOGICAL003")
                    .fileHash("hash789")
                    .originalName("todelete.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(true)
                    .isActive(false)
                    .storagePath("/storage/todelete.pdf")
                    .uploadedBy(adminUser.getId())
                    .build());

            mockMvc.perform(delete("/api/files/" + deletedFile.getId() + "/permanent")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }
    }
}
