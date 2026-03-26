# Delivery Acceptance / Project Architecture Audit (Latest)

Project: `Anju Accompanying Medical Appointment Operation Management System`  
Scope: current workspace static audit + runnable-path verification (without Docker execution)  
Date: 2026-03-26

## Environment Limits

- Docker commands were not executed per mandatory constraint: "Do not start Docker or related commands".
- Local Maven is unavailable in this sandbox (`mvn: command not found`), so runtime and test execution results are marked with **Currently Confirmed / Unconfirmed** boundaries.
- Evidence: `README.md:7`, `README.md:60`, shell output from `mvn -v`.

---

## 1) Hard Thresholds

### 1.1 Can the delivery run and be verified?

#### 1.1.a Startup/Execution instructions are clear
- Conclusion: **Pass**
- Reason: README provides startup, stop, logs, service URLs, health checks, and test commands.
- Evidence: `README.md:5`, `README.md:7`, `README.md:18`, `README.md:56`, `README.md:214`
- Reproduction steps:
  1. Read quick start and health sections in `README.md`.
  2. Execute listed commands in a Docker-enabled local machine.

#### 1.1.b Can start without core code changes
- Conclusion: **Pass (Currently Confirmed by static evidence)**
- Reason: Complete Dockerized runtime path is present (compose + Dockerfile + entrypoint + init SQL), with no documented source edits required.
- Evidence: `docker-compose.yml:1`, `Dockerfile:1`, `docker-entrypoint.sh:1`, `init.sql:1`
- Reproduction steps:
  1. `docker compose up -d`
  2. `docker compose ps`
  3. `curl http://localhost:8080/actuator/health`

#### 1.1.c Actual runtime matches instructions
- Conclusion: **Unconfirmed (Environment Limits)**
- Reason: Runtime test execution was blocked by audit constraints (no Docker start) and missing local Maven binary.
- Evidence: `README.md:60`; shell output: `zsh:1: command not found: mvn`
- Reproduction steps:
  1. `docker compose up -d`
  2. `docker compose exec anju-backend mvn test`
  3. Compare actual endpoint behavior with README examples.

### 1.2 Prompt Theme Alignment

#### 1.2.a Implementation is centered on prompt business domains
- Conclusion: **Pass**
- Reason: Domain APIs/services cover auth, property, appointment, finance, file, audit/export.
- Evidence: `README.md:152`, `README.md:168`, `README.md:174`, `README.md:187`, `README.md:193`; controllers under `anju-backend/src/main/java/com/anju/controller/`
- Reproduction steps:
  1. Inspect domain controllers/services.
  2. Map each to prompt domain statements.

#### 1.2.b Core problem is not replaced/ignored
- Conclusion: **Partial**
- Reason: Main theme is preserved, but some prompt-mandated details are still partial (refund non-original method path, strict instant upload behavior, composite status+time indexes).
- Evidence: refund channel fixed to original `anju-backend/src/main/java/com/anju/service/FinancialService.java:134`; chunk init always `deduplicated(false)` `anju-backend/src/main/java/com/anju/service/FileService.java:469`; index definitions lacking status+time composite `anju-backend/src/main/java/com/anju/entity/Appointment.java:12`, `anju-backend/src/main/java/com/anju/entity/Transaction.java:15`
- Reproduction steps:
  1. Trace refund flow in service/controller.
  2. Trace chunk init response and dedup decision timing.
  3. Review entity index declarations.

---

## 2) Delivery Completeness

### 2.1 Coverage of core prompt requirements

#### Property Domain
- Conclusion: **Pass**
- Reason: CRUD/listing workflow, submit/approve/reject, vacancy management, rent/deposit, status lifecycle, and compliance fields are implemented.
- Evidence: `README.md:168`; `anju-backend/src/main/java/com/anju/service/PropertyService.java:94`; `anju-backend/src/main/java/com/anju/service/VacancyPeriodService.java:41`; `anju-backend/src/main/java/com/anju/entity/Property.java:46`
- Reproduction steps:
  1. Create/update property.
  2. Submit for review then approve/reject.
  3. Create/query vacancy periods for a property.

#### Appointment Domain
- Conclusion: **Pass**
- Reason: Standard duration enforcement (15/30/60/90), staff/resource conflict detection, 24h policy, max 2 reschedules, state transitions, and 15-min stale release are implemented.
- Evidence: constants/policies `anju-backend/src/main/java/com/anju/service/AppointmentService.java:44`; conflict checks `.../AppointmentService.java:637`, `...:651`; reschedule limit `...:218`; auto-release `...:482`
- Reproduction steps:
  1. Create conflicting staff/resource appointments and verify rejection.
  2. Attempt >2 reschedules.
  3. Validate auto-cancel of stale pending appointments.

#### Financial Domain
- Conclusion: **Partial**
- Reason: Bookkeeping payments/refunds/settlement/export/invoice lifecycle exist, but refund channel selection for original/non-original methods is not exposed (refund always inherits original channel).
- Evidence: payment/refund APIs `anju-backend/src/main/java/com/anju/controller/FinancialController.java:27`, `...:41`; refund implementation `anju-backend/src/main/java/com/anju/service/FinancialService.java:128`, `...:134`; settlement/invoice `.../SettlementService.java:45`, `...:149`
- Reproduction steps:
  1. Create payment with one channel.
  2. Request refund and verify saved refund channel equals original payment channel.

#### File Domain
- Conclusion: **Partial**
- Reason: Chunked upload, resume-by-upload-id, throttling, preview, version rollback, recycle bin retention are present; hash instant-upload at init stage is not fully implemented (init always returns non-deduplicated).
- Evidence: init upload response `anju-backend/src/main/java/com/anju/service/FileService.java:466`; chunk/complete flow `...:474`, `...:506`; preview support `anju-backend/src/main/java/com/anju/controller/FileController.java:303`; cleanup policy `.../FileService.java:358`
- Reproduction steps:
  1. Call `/api/files/upload/init` twice with same hash.
  2. Observe `deduplicated=false` at init response.
  3. Complete upload and verify dedup only occurs in simple/complete path.

#### Security & Compliance requirements
- Conclusion: **Pass (with residual risks)**
- Reason: local auth, password complexity, BCrypt hashing, field encryption/masking, audit logs, idempotency for finance/import are implemented; a few security gaps remain (detailed in Security chapter).
- Evidence: password rule `anju-backend/src/main/java/com/anju/security/PasswordValidator.java:13`; BCrypt `anju-backend/src/main/java/com/anju/config/SecurityConfig.java:34`; encrypted fields `anju-backend/src/main/java/com/anju/entity/User.java:42`; audit logging `anju-backend/src/main/java/com/anju/service/AuditLogService.java:35`; persistent idempotency `anju-backend/src/main/java/com/anju/entity/IdempotencyEntry.java:10`
- Reproduction steps:
  1. Validate weak password rejection.
  2. Verify 401/403 behavior on protected endpoints.
  3. Create a mutating action and check audit log record.

### 2.2 0-to-1 Delivery Form

#### 2.2.a Complete project structure and docs
- Conclusion: **Pass**
- Reason: Complete Spring monolith with layered modules, Docker stack, SQL init, tests, and README.
- Evidence: `README.md:27`; `docker-compose.yml:1`; `anju-backend/pom.xml:1`
- Reproduction steps:
  1. Inspect tree and verify each layer exists.

#### 2.2.b Mocks/hardcoding handling
- Conclusion: **Partial**
- Reason: `WECHAT_MOCK` channel is acceptable for bookkeeping-only requirement, but there is still accidental mock-in-production risk because it is a first-class enum value without profile guard.
- Evidence: `anju-backend/src/main/java/com/anju/entity/Transaction.java:163`; `anju-backend/src/main/java/com/anju/dto/PaymentRequest.java:29`
- Reproduction steps:
  1. Submit payment with `WECHAT_MOCK`.
  2. Verify it persists like a regular production channel.

---

## 3) Engineering & Architecture Quality

### 3.1 Module structure reasonableness

#### 3.1.a Clear responsibilities and separation
- Conclusion: **Pass**
- Reason: Controller/service/repository/entity/security/exception layers are cleanly separated.
- Evidence: `README.md:31`, `README.md:36`, `README.md:45`, `README.md:49`
- Reproduction steps:
  1. Traverse packages to verify dependency flow.

#### 3.1.b Redundant/unnecessary files, single-file stacking
- Conclusion: **Partial**
- Reason: No single-file overstacking problem, but several classes still contain commented-out Lombok blocks causing maintenance noise.
- Evidence: e.g. `anju-backend/src/main/java/com/anju/entity/User.java:15`; `anju-backend/src/main/java/com/anju/entity/Transaction.java:9`
- Reproduction steps:
  1. Search for commented imports/annotations across entity/dto classes.

### 3.2 Maintainability & scalability

#### 3.2.a Expandability vs hardcoded temporary logic
- Conclusion: **Pass**
- Reason: Domain logic is mostly parameterized and persistence-backed (including idempotency), with transactional boundaries in key operations.
- Evidence: persistent idempotency `anju-backend/src/main/java/com/anju/service/IdempotencyService.java:23`; transaction boundaries in appointment/file/finance services
- Reproduction steps:
  1. Review service constructors/dependencies and transaction annotations.

#### 3.2.b Coupling/chaos risk
- Conclusion: **Partial**
- Reason: Overall coupling is acceptable, but a few edge behaviors remain policy-fragile (Nacos startup fallback, missing composite index strategy for status+time heavy queries).
- Evidence: Nacos fallback starts anyway `docker-entrypoint.sh:22`; appointment/transaction indexes `.../Appointment.java:12`, `.../Transaction.java:15`
- Reproduction steps:
  1. Inspect startup script behavior when Nacos remains unhealthy.
  2. Inspect index declarations vs query patterns.

---

## 4) Engineering Details & Professionalism

### 4.1 Error handling, logging, validation, API design

#### 4.1.a Error handling reliability
- Conclusion: **Pass**
- Reason: Centralized handler maps 400/401/403/404/409/500 with sanitized references for internal errors.
- Evidence: `anju-backend/src/main/java/com/anju/exception/GlobalExceptionHandler.java:26`, `...:33`, `...:47`, `...:132`, `...:205`
- Reproduction steps:
  1. Trigger representative exceptions and validate HTTP status/message consistency.

#### 4.1.b Logging professionalism
- Conclusion: **Pass**
- Reason: Plaintext credential logging issues in seeding are addressed; logs now explicitly avoid printing temporary credentials.
- Evidence: `anju-backend/src/main/java/com/anju/config/DataSeeder.java:114`, `...:133`
- Reproduction steps:
  1. Start service with seeding profile.
  2. Inspect startup logs for secret exposure.

#### 4.1.c Input and boundary validation
- Conclusion: **Partial**
- Reason: Validation is strong in many DTOs and CSV import, but some boundary controls remain weak (e.g., paging params not range-constrained).
- Evidence: paging accepts raw ints `anju-backend/src/main/java/com/anju/controller/AppointmentController.java:32`; CSV validation strong `.../ImportExportService.java:186`
- Reproduction steps:
  1. Call listing endpoint with extreme `size/page` values.
  2. Observe behavior and potential performance risk.

### 4.2 Product-grade realism

#### 4.2.a Real service vs demo
- Conclusion: **Pass (with caveats)**
- Reason: Architecture, authz, auditing, schedulers, import/export, and throttling indicate product-oriented implementation; residual gaps are mostly edge completeness/performance/security-hardening details.
- Evidence: scheduler `anju-backend/src/main/java/com/anju/scheduler/AppointmentScheduler.java:20`; audit/service modules and domain controllers.
- Reproduction steps:
  1. Review end-to-end domain flow from controller to repository.

---

## 5) Requirement Understanding & Adaptation

### 5.1 Business goals and implicit constraints fidelity

- Conclusion: **Partial**
- Reason: Understanding is strong and most constraints are correctly encoded; remaining mismatches are mainly (a) refund non-original method option, (b) strict instant-upload semantics at init, and (c) index strategy explicitly targeting status+time range.
- Evidence: `anju-backend/src/main/java/com/anju/service/FinancialService.java:134`; `anju-backend/src/main/java/com/anju/service/FileService.java:469`; `anju-backend/src/main/java/com/anju/entity/Appointment.java:12`
- Reproduction steps:
  1. Validate refund path cannot pick alternate return channel.
  2. Validate upload init returns non-deduplicated regardless of existing hash.
  3. Compare query patterns and index definitions.

---

## 6) Aesthetics (Frontend only)

### 6.1 Visual/interaction quality
- Conclusion: **N/A**
- Reason: Delivery is backend API project; no frontend application/UI was delivered.
- Evidence: backend-only structure `README.md:27`
- Reproduction steps:
  1. Verify repository contains no frontend app/module.

---

## Testing Coverage Evaluation (Static Audit)

### Overview

- Framework/tooling: Spring Boot Test, JUnit 5, Mockito, MockMvc.
- Entry point in docs: `mvn test` / Docker test commands.
- Static execution boundary: tests were not run in this sandbox due environment limits.
- Evidence: `anju-backend/pom.xml:94`, `README.md:56`, shell output (`mvn` unavailable).

### Coverage Mapping Table

| Requirement / Risk | Test Case | Assertion Evidence | Coverage Status |
|---|---|---|---|
| 401 Unauthorized | `ApiStatusCodeIntegrationTest.java:102` | `status().isUnauthorized()` at `...:107`, `...:115`, `...:125` | Full |
| 403 Forbidden / RBAC | `ApiStatusCodeIntegrationTest.java:173`, `SecurityIntegrationTest_v2.java:225` | `status().isForbidden()` at `...:187`, `...:235`, `...:288` | Full |
| 404 Not Found | `ApiStatusCodeIntegrationTest.java:214` | `status().isNotFound()` at `...:220`, `...:229`, `...:238` | Full |
| 409 Conflict | `ApiStatusCodeIntegrationTest.java:256` | `status().isConflict()` at `...:277` | Basic |
| Appointment duration standards | `AppointmentServiceTest.java:57` | duration assertions at `...:81`, `...:107`, `...:132`, `...:157` | Full |
| 24h policy + penalty | `AppointmentServiceTest.java:203` | penalty assertions `...:215`, `...:233`, `...:258` | Full |
| Max 2 reschedules | `AppointmentServiceTest.java:267` | exception/assertions `...:284`, `...:316` | Full |
| Auto-release 15 min | `AppointmentServiceTest.java:427` | stale cancel assertion `...:445` | Full |
| IDOR appointment | `SecurityIntegrationTest_v2.java:296` | forbidden assertion `...:311`, `...:329` | Basic |
| File access control | `FileCriticalPathTest.java:431` | owner/non-owner assertions at `...:455`, `...:475` | Basic |
| Import/export validation rules | `ImportExportServiceTest.java:41` | required/enum/duration/date checks `...:73`, `...:91`, `...:109`, `...:147` | Full |
| Idempotency behavior | `IdempotencyServiceTest.java:19` | duplicate/cache/remove checks `...:29`, `...:48`, `...:59` | Full |
| Concurrency boundary | `AppointmentServiceTest.java:469` | test disabled | Insufficient |
| Pagination boundary | no focused test found | N/A | Missing |
| Transaction rollback boundary | no robust enabled rollback test found | N/A | Missing |

### Security Coverage Audit (Auth, IDOR, Data Isolation)

- Route-level auth/RBAC: **Strong** (multiple integration tests).
- Object-level authorization: **Improved and mostly covered**, but still missing explicit integration tests for some newer endpoints (e.g., import result by idempotency key ownership).
- Data isolation: core appointment/file ownership checks are present in service paths used by endpoints.
- Evidence: appointment owner filtering `anju-backend/src/main/java/com/anju/service/AppointmentService.java:320`; file ownership checks `.../FileService.java:374`, `...:396`; import result endpoint lacks owner binding `.../AppointmentImportExportController.java:64`, `.../ImportExportService.java:172`

### Overall testing sufficiency judgment

- Conclusion: **Partial**
- Reason: Core happy-path and major HTTP/security basics are covered, but boundary/race-condition confidence remains limited due disabled concurrency tests and missing pagination/transaction rollback focused coverage.

---

## Security & Logs Findings

1) **Import result retrieval lacks object-level ownership check (idempotency-key based lookup)**  
Severity: **Medium**  
Reason: Any authorized admin/frontline with a known key may fetch another user’s import result.  
Evidence: `anju-backend/src/main/java/com/anju/controller/AppointmentImportExportController.java:64`; `anju-backend/src/main/java/com/anju/service/ImportExportService.java:172`  
Reproduction:
  1. User A performs import with known `Idempotency-Key`.
  2. User B calls `/api/admin/appointments/import/result/{key}`.
  3. Verify whether A’s result is exposed.

2) **Nacos readiness fallback may violate strict dependency startup policy**  
Severity: **Low**  
Reason: Entrypoint waits for Nacos but starts app anyway after timeout; this weakens strict “Nacos must be up first” operational guarantee.  
Evidence: `docker-entrypoint.sh:8`, `docker-entrypoint.sh:22`  
Reproduction:
  1. Start backend with Nacos intentionally unavailable.
  2. Observe container still starts app after timeout.

3) **Financial refund method flexibility is incomplete (original/non-original requirement)**  
Severity: **Medium**  
Reason: Refund channel is hard-bound to original transaction channel.  
Evidence: `anju-backend/src/main/java/com/anju/service/FinancialService.java:134`  
Reproduction:
  1. Create original payment.
  2. Attempt refund via alternate method (no API field/path).
  3. Observe refund always uses original channel.

4) **Composite status+time index strategy not fully aligned with prompt performance requirement**  
Severity: **Medium**  
Reason: Indexed fields exist, but explicit composite indexes for status + time-range heavy queries are not consistently modeled on key tables.
Evidence: `anju-backend/src/main/java/com/anju/entity/Appointment.java:12`; `anju-backend/src/main/java/com/anju/entity/Transaction.java:15`  
Reproduction:
  1. Review entity indexes.
  2. Compare against common `status + time range` query patterns.

5) **Instant-upload semantics at init phase are partial**  
Severity: **Low**  
Reason: Init endpoint always returns `deduplicated=false`; dedup is deferred to later flow.
Evidence: `anju-backend/src/main/java/com/anju/service/FileService.java:469`  
Reproduction:
  1. Call init upload repeatedly with same hash.
  2. Verify no immediate instant-upload short-circuit at init.

---

## Final Acceptance Judgment

- **Overall result: Partial (close to production-ready, but not fully accepted against strict prompt contract).**
- **Currently Confirmed:** architecture quality, core domain implementation breadth, auth/RBAC baseline, persistent idempotency, audit/logging hygiene improvements.
- **Currently Unconfirmed (environment-limited):** end-to-end runtime behavior and test pass status.

## Local Reproduction Command Set

```bash
# from repo root
docker compose up -d
docker compose ps
curl http://localhost:8080/actuator/health

# run tests in container
docker compose exec anju-backend mvn test

# optional: inspect logs
docker compose logs -f anju-backend
```
