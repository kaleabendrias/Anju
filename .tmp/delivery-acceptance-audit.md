# Delivery Acceptance / Project Architecture Audit

Project: `Anju Accompanying Medical Appointment Operation Management System`  
Audit scope: static code + runnable instructions review in current workspace  
Audit date: 2026-03-26

## Environment Limits

- Docker-related execution was intentionally not performed per audit constraint ("Do not start Docker or related commands").
- Local Maven execution is unavailable in this environment (`mvn: command not found`), so runtime and test execution are **Unconfirmed by execution**.
- Therefore, runtime conclusions are split into **Currently Confirmed (static evidence)** and **Unconfirmed (requires local run)**.

---

## 1. Hard Thresholds

### 1.1 Runnability & Verifiability

#### 1.1.a Clear startup/run instructions
- Conclusion: **Pass**
- Reason: Root README provides startup, logs, health check, and service addresses.
- Evidence: `README.md:5`, `README.md:7`, `README.md:18`, `README.md:214`
- Reproduction steps:
  1. Read `README.md` quick start and health sections.
  2. Follow listed commands locally in a Docker-capable environment.

#### 1.1.b Can run without core code modification
- Conclusion: **Pass (static)**
- Reason: Delivery includes Docker Compose, Dockerfile, SQL init script, and env defaults, with no required code edits documented.
- Evidence: `docker-compose.yml:1`, `Dockerfile:1`, `init.sql:1`, `README.md:7`
- Reproduction steps:
  1. Ensure Docker/Compose installed locally.
  2. Run `docker compose up -d` from repo root.
  3. Verify service health via README endpoints.

#### 1.1.c Actual run result matches instructions
- Conclusion: **Unconfirmed (Environment Limits)**
- Reason: Runtime was not executed (Docker prohibited), and local Maven missing in audit environment.
- Evidence: `README.md:7`, `README.md:60`; command output `mvn: command not found` (audit shell)
- Reproduction steps:
  1. `docker compose up -d`
  2. `curl http://localhost:8080/actuator/health`
  3. `docker compose exec anju-backend mvn test`

### 1.2 Prompt Theme Alignment

#### 1.2.a Centered on prompt business goals
- Conclusion: **Pass**
- Reason: Codebase is domain-organized around property, appointment, finance, file, auth/audit APIs.
- Evidence: `README.md:152`, `README.md:168`, `README.md:174`, `README.md:187`, `README.md:193`, `anju-backend/src/main/java/com/anju/controller/`
- Reproduction steps:
  1. Inspect controllers/services by domain package.
  2. Cross-check APIs against prompt domain list.

#### 1.2.b No major theme substitution/ignoring core problem
- Conclusion: **Partial**
- Reason: Overall theme is aligned, but several prompt-critical capabilities are only partial/missing (detailed in section 2.1).
- Evidence: missing appointment import/export endpoints (`ImportExportService` exists but no controller exposure) `anju-backend/src/main/java/com/anju/service/ImportExportService.java:94`, `anju-backend/src/main/java/com/anju/service/ImportExportService.java:336`; only property import endpoint exists `anju-backend/src/main/java/com/anju/controller/DataExchangeController.java:21`
- Reproduction steps:
  1. Search REST mappings for appointment import/export.
  2. Compare with required import/export scope in prompt.

---

## 2. Delivery Completeness

### 2.1 Core Requirement Coverage

#### Property domain
- Conclusion: **Partial**
- Reason: CRUD/status/review/vacancy/rent-deposit fields are implemented; compliance-specific validation workflow is represented in entity but not fully operationalized via dedicated API/service flow.
- Evidence: property workflow `anju-backend/src/main/java/com/anju/service/PropertyService.java:94`; vacancy management `anju-backend/src/main/java/com/anju/service/VacancyPeriodService.java:41`; compliance fields only on entity `anju-backend/src/main/java/com/anju/entity/Property.java:64`
- Reproduction steps:
  1. Call property create/update/submit/approve/reject APIs.
  2. Verify no dedicated compliance validation/approval endpoints beyond generic status review.

#### Appointment domain
- Conclusion: **Partial**
- Reason: Standard durations/conflict checks/24h + max-reschedule/auto-release/state transitions exist, but dedicated "available slot maintenance" is not clearly modeled as a first-class resource/API.
- Evidence: duration + conflict + penalty + auto-cancel `anju-backend/src/main/java/com/anju/service/AppointmentService.java:487`, `...:588`, `...:555`, `...:441`; state transitions `...:308`, `...:338`, `...:368`, `...:398`
- Reproduction steps:
  1. Create conflicting appointments and verify 409/BusinessException.
  2. Attempt reschedule >2 times and inside/outside 24h.
  3. Check scheduler auto-cancel behavior after 15 minutes pending.

#### Financial domain
- Conclusion: **Partial**
- Reason: Bookkeeping transactions, idempotent payment record, refunds, daily settlement, exceptions, invoice lifecycle, export endpoints are present; explicit support for original vs non-original refund channel choice is not exposed.
- Evidence: payment/refund `anju-backend/src/main/java/com/anju/service/FinancialService.java:42`, `...:88`; refund uses original channel `...:134`; settlement/invoice `anju-backend/src/main/java/com/anju/service/SettlementService.java:45`, `...:119`, `...:149`, `...:185`; export endpoints `anju-backend/src/main/java/com/anju/controller/FinancialController.java:179`, `...:194`
- Reproduction steps:
  1. Record payment with idempotency key.
  2. Process refund and verify duplicate handling.
  3. Generate settlement and issue/reject invoice.

#### File domain
- Conclusion: **Partial**
- Reason: Chunk upload, dedup, versioning/rollback, recycle bin retention, preview, cleanup tasks, and throttle exist; resumable/instant upload semantics are only partial (init phase does not short-circuit via existing hash for instant upload).
- Evidence: init/chunk/complete `anju-backend/src/main/java/com/anju/service/FileService.java:422`, `...:461`, `...:492`; dedup in simple upload `...:93`; rollback `...:161`; recycle bin retention `...:213`; cleanup `...:358`; preview types `anju-backend/src/main/java/com/anju/controller/FileController.java:303`
- Reproduction steps:
  1. Perform `/api/files/upload/init` + chunk uploads + complete.
  2. Upload same content via `/api/files/upload` and verify dedup behavior.
  3. Soft delete and validate expiration/cleanup.

#### Security/compliance requirements
- Conclusion: **Partial**
- Reason: Local auth, password complexity, strong hashing, audit logs, idempotency checks, masking/encryption components are present; several access-control and operational-security gaps remain (see security chapter).
- Evidence: password rule `anju-backend/src/main/java/com/anju/security/PasswordValidator.java:13`; BCrypt strength 10 `anju-backend/src/main/java/com/anju/config/SecurityConfig.java:34`; encrypted fields `anju-backend/src/main/java/com/anju/entity/User.java:42`; audit logs `anju-backend/src/main/java/com/anju/service/AuditLogService.java:35`; idempotency `anju-backend/src/main/java/com/anju/service/IdempotencyService.java:21`
- Reproduction steps:
  1. Test password policy via user creation endpoint.
  2. Verify unauthorized/forbidden responses for protected routes.
  3. Inspect audit log entries for mutations.

### 2.2 0-to-1 Project Form Completeness

#### 2.2.a Complete project structure (not snippets)
- Conclusion: **Pass**
- Reason: Monolithic Spring Boot project with layered modules, Dockerization, SQL bootstrap, tests, and docs.
- Evidence: `README.md:27`, `docker-compose.yml:1`, `anju-backend/pom.xml:1`
- Reproduction steps:
  1. Review root tree and package layout.
  2. Confirm build/test config and boot entry.

#### 2.2.b Mock/hardcode replacing real logic without explanation
- Conclusion: **Partial**
- Reason: Mock payment channel exists and is acceptable per prompt (no external gateway required), but there is risk of accidental production usage since channel is part of normal enum and not environment-guarded.
- Evidence: `anju-backend/src/main/java/com/anju/entity/Transaction.java:163`; API accepts channel directly `anju-backend/src/main/java/com/anju/dto/PaymentRequest.java:29`
- Reproduction steps:
  1. Submit payment with `WECHAT_MOCK` channel.
  2. Observe that it is stored as normal transaction.

#### 2.2.c Basic documentation present
- Conclusion: **Pass**
- Reason: README includes quick start, API examples, domain descriptions, troubleshooting.
- Evidence: `README.md:5`, `README.md:96`, `README.md:224`
- Reproduction steps:
  1. Follow README sections in order.

---

## 3. Engineering & Architecture Quality

### 3.1 Structure & Module Division

#### 3.1.a Clear responsibilities and package boundaries
- Conclusion: **Pass**
- Reason: Standard layered architecture (`controller`/`service`/`repository`/`entity`/`security`/`exception`) with domain-oriented services.
- Evidence: `README.md:31`, `README.md:36`, `README.md:45`, `README.md:49`; package files under `anju-backend/src/main/java/com/anju/`
- Reproduction steps:
  1. Inspect class placement and dependency direction.

#### 3.1.b Redundant/unnecessary files or clutter
- Conclusion: **Partial**
- Reason: Many classes contain commented Lombok remnants and duplicated test tracks (`FinanceCriticalPathTest` and `FinancialCriticalPathTest`), increasing noise.
- Evidence: commented imports/annotations e.g. `anju-backend/src/main/java/com/anju/entity/Property.java:13`, `...:25`; duplicate-style test files `anju-backend/src/test/java/com/anju/service/FinanceCriticalPathTest.java:1`, `anju-backend/src/test/java/com/anju/service/FinancialCriticalPathTest.java:1`
- Reproduction steps:
  1. Scan for commented code blocks and near-duplicate tests.

### 3.2 Maintainability & Scalability

#### 3.2.a Hardcoding/coupling risks
- Conclusion: **Partial**
- Reason: Several hardcoded business constants are acceptable, but in-memory idempotency cache is non-persistent and unsuitable for multi-instance/offline restart-safe operations.
- Evidence: in-memory map `anju-backend/src/main/java/com/anju/service/IdempotencyService.java:19`; TTL logic `...:47`
- Reproduction steps:
  1. Restart app and repeat same idempotency key request; observe duplicate protection resets.

#### 3.2.b Expansion room vs temporary implementation
- Conclusion: **Partial**
- Reason: Domain separation supports extension, but key security/access checks are inconsistently enforced across methods, indicating risk under feature growth.
- Evidence: no ownership check in file version listing `anju-backend/src/main/java/com/anju/service/FileService.java:374`; no ownership check in restore `...:380`
- Reproduction steps:
  1. Use non-owner account to call affected endpoints and verify cross-resource behavior.

---

## 4. Engineering Details & Professionalism

### 4.1 Errors, Logs, Validation, API Design

#### 4.1.a Error handling quality
- Conclusion: **Pass (with caveats)**
- Reason: Centralized exception mapping with proper 400/401/403/404/409 and sanitized generic 500 with reference IDs.
- Evidence: `anju-backend/src/main/java/com/anju/exception/GlobalExceptionHandler.java:26`, `...:33`, `...:47`, `...:132`, `...:205`
- Reproduction steps:
  1. Trigger each exception type via invalid requests.
  2. Confirm response code/message mapping.

#### 4.1.b Logging professionalism
- Conclusion: **Partial**
- Reason: Structured logging exists broadly; however, `DataSeeder` logs temporary/default passwords, which is a security anti-pattern.
- Evidence: password logging `anju-backend/src/main/java/com/anju/config/DataSeeder.java:112`, `...:114`, `...:133`, `...:135`
- Reproduction steps:
  1. Start app in docker/dev profile with seeding enabled.
  2. Inspect startup logs for plaintext password leakage.

#### 4.1.c Critical input/boundary validation
- Conclusion: **Partial**
- Reason: Many DTO and service validations exist; some business inputs are under-validated (e.g., patient name not `@NotBlank` at API DTO layer, pagination boundaries not constrained).
- Evidence: appointment DTO lacks `@NotBlank` for patientName `anju-backend/src/main/java/com/anju/dto/AppointmentCreateRequest.java:40`; pagination params directly accepted `anju-backend/src/main/java/com/anju/controller/AppointmentController.java:32`
- Reproduction steps:
  1. Submit appointment with blank patient name.
  2. Submit extreme pagination values and observe behavior.

### 4.2 Product-grade vs Demo-grade

#### 4.2.a Real service characteristics
- Conclusion: **Partial**
- Reason: Presence of auth/RBAC/audit/schedulers/domain services indicates product intent, but critical security holes and partially exposed features keep it below production acceptance.
- Evidence: auth + RBAC `anju-backend/src/main/java/com/anju/config/SecurityConfig.java:52`; scheduled tasks `anju-backend/src/main/java/com/anju/scheduler/AppointmentScheduler.java:20`, `anju-backend/src/main/java/com/anju/scheduler/FileCleanupTask.java:20`; security gaps in section "Security & Logs Findings"
- Reproduction steps:
  1. Run integration tests.
  2. Execute targeted IDOR scenarios documented below.

---

## 5. Requirement Understanding & Adaptation

### 5.1 Business goal/constraint fidelity

- Conclusion: **Partial**
- Reason: Most domain semantics were understood, but key constraints are weakened/changed:
  - appointment status-filter query bypasses object-level isolation,
  - file version/restore flows miss ownership checks,
  - appointment import/export exists in service but not fully exposed in API,
  - refund channel flexibility (original/non-original) not explicit.
- Evidence: `anju-backend/src/main/java/com/anju/controller/AppointmentController.java:43`, `anju-backend/src/main/java/com/anju/service/AppointmentService.java:297`, `anju-backend/src/main/java/com/anju/service/FileService.java:374`, `...:380`, `anju-backend/src/main/java/com/anju/service/ImportExportService.java:94`
- Reproduction steps:
  1. Execute unauthorized cross-owner read scenarios.
  2. Search for missing endpoint mappings for appointment import/export.

---

## 6. Aesthetics (Full-stack / Front-end)

### 6.1 Visual/interaction quality
- Conclusion: **N/A**
- Reason: Delivery is backend API only; no frontend UI in scope.
- Evidence: backend-only structure in `README.md:27`, no frontend assets/app package
- Reproduction steps:
  1. Verify repository contains only backend services/config/tests.

---

## Testing Coverage Evaluation (Static Audit)

### Overview

- Framework/stack: JUnit 5 + Mockito + Spring Boot Test + MockMvc.
- Main test entry: `mvn test` (documented in README).
- Important caveat: Surefire excludes several integration/critical-path suites by default.
- Evidence: test command docs `README.md:56`; surefire exclusions `anju-backend/pom.xml:177`

### Coverage Mapping Table

| Requirement / Risk | Test Case Evidence | Assertion Evidence | Coverage Status |
|---|---|---|---|
| Auth 401 | `ApiStatusCodeIntegrationTest.java:86` | `status().isUnauthorized()` at `...:91` | Full |
| RBAC 403 | `SecurityIntegrationTest_v2.java:225` | `status().isForbidden()` at `...:235` | Full |
| 404 handling | `ApiStatusCodeIntegrationTest.java:198` | `status().isNotFound()` at `...:204` | Full |
| 409 conflict | `ApiStatusCodeIntegrationTest.java:240` | `status().isConflict()` at `...:261` | Full |
| Appointment duration 15/30/60/90 | `AppointmentServiceTest.java:57` | duration assertions at `...:81`, `...:107`, `...:132`, `...:157` | Full |
| Conflict detection | `AppointmentServiceTest.java:324` | throws conflict at `...:344` | Basic |
| 24h penalty policy | `AppointmentServiceTest.java:203` | penalty assertions at `...:215`, `...:233` | Full |
| Max 2 reschedules | `AppointmentServiceTest.java:267` | exception/assertions at `...:284`, `...:316` | Full |
| Auto release after 15 min | `AppointmentServiceTest.java:399` | cancelled assertion at `...:417` | Full |
| Finance idempotency | `FinancialServiceTest.java:106` | no extra save at `...:119` | Basic |
| Import/export validation | `ImportExportServiceTest.java:41` | required/enum/date assertions `...:73`, `...:91`, `...:145` | Basic |
| IDOR (appointment/file) | `SecurityIntegrationTest_v2.java:293` | forbidden assertions `...:311`, `...:423` | Basic |
| Pagination boundary | `AppointmentController.java` supports paging, but no dedicated tests found | N/A | Missing |
| Concurrency boundary | Concurrency tests exist but disabled `AppointmentServiceTest.java:437`, `FinanceCriticalPathTest.java:259`, `FileCriticalPathTest.java:380` | Disabled | Missing (effective) |
| Transaction rollback boundary | tests disabled `FinanceCriticalPathTest.java:306` | Disabled | Missing (effective) |

### Security Coverage Audit (Auth, IDOR, Data Isolation)

- Strengths:
  - Auth/RBAC integration tests cover many route-level permissions.
  - Some IDOR tests for appointment/file access are present.
- Gaps:
  - No tests catch data leakage via appointment `status` filter path returning global results.
  - No tests for unauthorized `restoreFile` or `getFileVersions` ownership enforcement.
  - Several critical-path security tests are disabled or excluded from default test run.
- Evidence: leakage path `AppointmentController.java:43` + `AppointmentService.java:297`; file access gaps `FileService.java:374`, `FileService.java:380`; disabled/excluded tests `AppointmentServiceTest.java:437`, `FinanceCriticalPathTest.java:62`, `anju-backend/pom.xml:177`

### Overall Testing Judgment

- Conclusion: **Partial (Insufficient for major-defect prevention)**
- Reason: Tests are numerous and well-structured, but effective coverage is reduced by disabled/excluded suites and misses at least one critical IDOR/data-isolation defect class.

---

## Security & Logs Findings

### Blocker

1) **IDOR/Data isolation bypass on appointment status queries**
- Severity: **Blocker**
- Why: Any authenticated user can request `status`-filtered lists and receive all matching appointments, bypassing ownership scoping.
- Evidence: `anju-backend/src/main/java/com/anju/controller/AppointmentController.java:43`, `anju-backend/src/main/java/com/anju/service/AppointmentService.java:297`
- Reproduction:
  1. Login as `frontline` user A; create appointment.
  2. Login as `frontline` user B; call `GET /api/appointments?status=PENDING`.
  3. Observe A's records appear to B.

### High

2) **File ownership checks missing for version list and restore**
- Severity: **High**
- Why: `getFileVersions` and `restoreFile` do not validate owner/admin before action.
- Evidence: `anju-backend/src/main/java/com/anju/service/FileService.java:374`, `...:380`
- Reproduction:
  1. User A uploads file with `logicalId`.
  2. User B calls `/api/files/logical/{logicalId}/versions` and `/api/files/{id}/restore`.
  3. Observe cross-user access/operation succeeds if endpoint role checks pass.

3) **Sensitive password disclosure in startup logs**
- Severity: **High**
- Why: Seeder logs default/temporary passwords, violating secure logging principles.
- Evidence: `anju-backend/src/main/java/com/anju/config/DataSeeder.java:114`, `...:135`
- Reproduction:
  1. Run app under docker/dev profile with seeding.
  2. Inspect startup logs for plaintext passwords.

### Medium

4) **Appointment import/export API not fully delivered**
- Severity: **Medium**
- Why: Import/export service exists, but no controller endpoints for appointment CSV import/export workflow.
- Evidence: `anju-backend/src/main/java/com/anju/service/ImportExportService.java:94`, `...:336`; no matching controller mappings.
- Reproduction:
  1. Search for mappings exposing appointment import/export.
  2. Confirm only property import (`DataExchangeController`) and finance export endpoints.

5) **In-memory idempotency store not durable**
- Severity: **Medium**
- Why: Duplicate-prevention state is process-local and lost on restart.
- Evidence: `anju-backend/src/main/java/com/anju/service/IdempotencyService.java:19`
- Reproduction:
  1. Submit request with idempotency key.
  2. Restart service.
  3. Resubmit same key; observe duplicate accepted.

### Low

6) **Test execution config excludes important suites by default**
- Severity: **Low**
- Why: Several integration/critical tests are excluded in surefire, reducing CI confidence if unchanged.
- Evidence: `anju-backend/pom.xml:177`
- Reproduction:
  1. Run `mvn test` locally.
  2. Confirm excluded classes are not executed.

7) **Mock payment channel mixed in production enum without safeguard**
- Severity: **Low**
- Why: Acceptable per prompt, but should be profile-gated to avoid accidental production usage.
- Evidence: `anju-backend/src/main/java/com/anju/entity/Transaction.java:165`
- Reproduction:
  1. Submit payment using `WECHAT_MOCK`.
  2. Verify normal persistence.

---

## Final Acceptance Judgment

- **Overall conclusion: Partial / Not Ready for strict acceptance yet**
- **Key blockers/high risks** are concentrated in object-level authorization and sensitive logging.
- **Runtime verification** remains unconfirmed in this environment due stated limits, but static evidence is sufficient to identify material security and completeness defects.

## Suggested Fix Priority

1. Fix appointment status query data isolation (owner scoping under all filters).  
2. Add ownership checks for file version/restore operations.  
3. Remove plaintext password logging from seeding.  
4. Expose and secure appointment import/export endpoints (or remove dead service paths).  
5. Replace in-memory idempotency with persistent store (DB/Redis) for durability.
