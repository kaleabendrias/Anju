# Anju Backend - Test Coverage Report

## Coverage Mapping Table

| Requirement/Risk | Test Class + Method | Main Assertion | Coverage Status |
|-----------------|---------------------|----------------|----------------|
| **Authentication** ||||
| Unauthenticated request returns 401 | `ApiStatusCodeIntegrationTest.UnauthorizedTests.getPropertiesWithoutToken_returns401` | Status 401 for missing token | **Full** |
| Invalid JWT token returns 401 | `AuthSecurityIntegrationTest.JwtAuthenticationFilterTests.expiredJwtToken_rejected` | Status 401 for expired token | **Full** |
| Malformed JWT token returns 401 | `AuthSecurityIntegrationTest.JwtAuthenticationFilterTests.malformedJwtToken_rejected` | Status 401 for malformed token | **Full** |
| Invalid login credentials returns 401 | `ApiStatusCodeIntegrationTest.UnauthorizedTests.loginWithInvalidCredentials_returns401` | Status 401 for bad credentials | **Full** |
| **Authorization (RBAC)** ||||
| Wrong role returns 403 | `ApiStatusCodeIntegrationTest.ForbiddenTests.frontlineAccessingAdmin_returns403` | Status 403 for unauthorized role | **Full** |
| ADMIN accesses all endpoints | `AuthSecurityIntegrationTest.RbacAuthorizationTests.adminAccessesAllRoleEndpoints` | ADMIN succeeds on all role endpoints | **Full** |
| FRONTLINE restricted from finance | `AuthSecurityIntegrationTest.RbacAuthorizationTests.frontlineAccessesOwnRoleEndpoints` | Status 403 for finance access | **Full** |
| DISPATCHER confirms appointments | `AuthSecurityIntegrationTest.RbacAuthorizationTests.dispatcherConfirmsAppointments` | Appointment confirmed successfully | **Full** |
| **Object-Level Authorization (IDOR)** ||||
| User cannot view other's appointment | `AuthSecurityIntegrationTest.ObjectLevelAuthorizationTests.userCannotViewOthersAppointment` | Status 403 for cross-owner access | **Full** |
| User cannot cancel other's appointment | `AuthSecurityIntegrationTest.ObjectLevelAuthorizationTests.userCannotCancelOthersAppointment` | Status 403 for cancel | **Full** |
| User can view own appointment | `AuthSecurityIntegrationTest.ObjectLevelAuthorizationTests.userCanViewOwnAppointment` | Status 200 for own resource | **Full** |
| ADMIN views any appointment | `AuthSecurityIntegrationTest.ObjectLevelAuthorizationTests.adminCanViewAnyAppointment` | Status 200 for admin | **Full** |
| User cannot access other's file | `AuthSecurityIntegrationTest.ObjectLevelAuthorizationTests.userCannotAccessOthersFile` | Status 403 for file access | **Full** |
| **Secondary Password Verification** ||||
| Refund without secondary password fails | `AuthSecurityIntegrationTest.SecondaryPasswordVerificationTests.refundWithoutSecondaryPassword_fails` | Status 403 for missing secondary pass | **Full** |
| Permanent delete without secondary password fails | `AuthSecurityIntegrationTest.SecondaryPasswordVerificationTests.permanentDeleteWithoutSecondaryPassword_fails` | Status 403 for missing secondary pass | **Full** |
| **Status Codes** ||||
| 404 for non-existent property | `ApiStatusCodeIntegrationTest.NotFoundTests.getNonExistentProperty_returns404` | Status 404 for missing property | **Full** |
| 404 for non-existent appointment | `ApiStatusCodeIntegrationTest.NotFoundTests.getNonExistentAppointment_returns404` | Status 404 for missing appointment | **Full** |
| 404 for non-existent transaction | `ApiStatusCodeIntegrationTest.NotFoundTests.getNonExistentTransaction_returns404` | Status 404 for missing transaction | **Full** |
| 404 for non-existent file | `ApiStatusCodeIntegrationTest.NotFoundTests.getNonExistentFile_returns404` | Status 404 for missing file | **Full** |
| 409 for duplicate unique code | `ApiStatusCodeIntegrationTest.ConflictTests.duplicateUniqueCode_returns409` | Status 409 for duplicate property | **Full** |
| 409 for already approved property | `ApiStatusCodeIntegrationTest.ConflictTests.approveAlreadyApproved_returns409` | Status 409 for double approval | **Full** |
| 409 for already listed property | `ApiStatusCodeIntegrationTest.ConflictTests.rejectAlreadyListed_returns409` | Status 409 for invalid state transition | **Full** |
| **Finance - Payment** ||||
| Duplicate payment prevented | `FinanceCriticalPathTest.DuplicatePaymentPreventionTests.preventDuplicatePaymentWithSameIdempotencyKey` | Same ID returned, only 1 record | **Full** |
| Different idempotency keys allowed | `FinanceCriticalPathTest.DuplicatePaymentPreventionTests.allowPaymentWithDifferentIdempotencyKey` | Different IDs returned | **Full** |
| **Finance - Refund** ||||
| Non-refundable transaction rejected | `FinanceCriticalPathTest.RefundValidationTests.rejectRefundForNonRefundableTransaction` | BusinessException thrown | **Full** |
| Already refunded transaction rejected | `FinanceCriticalPathTest.RefundValidationTests.rejectRefundForFullyRefundedTransaction` | BusinessException thrown | **Full** |
| Non-existent transaction rejected | `FinanceCriticalPathTest.RefundValidationTests.rejectRefundForNonExistentTransaction` | BusinessException thrown | **Full** |
| Duplicate refund prevented | `FinanceCriticalPathTest.DuplicatePaymentPreventionTests.preventDuplicateRefundWithSameIdempotencyKey` | Only 1 refund created | **Full** |
| **Finance - Concurrency** ||||
| Concurrent idempotent requests handled | `FinanceCriticalPathTest.ConcurrencyTests.handleConcurrentIdempotentRequests` | Only 1 record created | **Full** |
| **File - Soft Delete** ||||
| Soft delete sets expiration | `FileCriticalPathTest.SoftDeleteTests.softDeleteSetsExpirationTime` | isDeleted=true, expiration set | **Full** |
| Reject double soft delete | `FileCriticalPathTest.SoftDeleteTests.rejectSoftDeleteOfAlreadyDeleted` | BusinessException thrown | **Full** |
| **File - Restore** ||||
| Restore deleted file | `FileCriticalPathTest.RestoreTests.restoreSoftDeletedFile` | isDeleted=false, expiration=null | **Full** |
| Reject restore of active file | `FileCriticalPathTest.RestoreTests.rejectRestoreOfActiveFile` | BusinessException thrown | **Full** |
| **File - Permanent Delete** ||||
| Permanent delete removes record | `FileCriticalPathTest.PermanentDeleteTests.permanentDeleteSoftDeletedFile` | record not exists | **Full** |
| Reject permanent delete of active file | `FileCriticalPathTest.PermanentDeleteTests.rejectPermanentDeleteOfActiveFile` | BusinessException thrown | **Full** |
| Reject without admin role | `FileCriticalPathTest.PermanentDeleteTests.rejectPermanentDeleteWithoutAdminRole` | BusinessException thrown | **Full** |
| Require secondary password | `FileCriticalPathTest.PermanentDeleteTests.requireSecondaryPasswordForPermanentDelete` | BusinessException for wrong password | **Full** |
| **File - Version** ||||
| New version on same logical ID | `FileCriticalPathTest.VersionTests.createNewVersionOnSameLogicalId` | 2 versions created | **Full** |
| Only one active version | `FileCriticalPathTest.VersionTests.onlyOneVersionActive` | 1 active version | **Full** |
| **File - Concurrency** ||||
| Concurrent delete requests idempotent | `FileCriticalPathTest.ConcurrencyTests.handleConcurrentDeleteRequests` | 1 success, others handled | **Full** |
| **File - Access Control** ||||
| User accesses own files | `FileCriticalPathTest.AccessControlTests.userAccessOwnFiles` | FileRecordResponse returned | **Full** |
| User blocked from others' files | `FileCriticalPathTest.AccessControlTests.userCannotAccessOtherFiles` | BusinessException thrown | **Full** |
| Admin accesses any file | `FileCriticalPathTest.AccessControlTests.adminCanAccessAnyFile` | FileRecordResponse returned | **Full** |
| **Repository Queries** ||||
| Staff conflict detection | `RepositoryQueryTest.AppointmentRepositoryTests.findConflictingStaffAppointments` | Conflict detected correctly | **Full** |
| Stale appointment detection | `RepositoryQueryTest.AppointmentRepositoryTests.findStaleAppointments` | Stale appointments found | **Full** |
| Idempotency key lookup | `RepositoryQueryTest.TransactionRepositoryTests.findByIdempotencyKey` | Transaction found by key | **Full** |
| Amount sum by type/range | `RepositoryQueryTest.TransactionRepositoryTests.sumAmountsByTypeAndTimeRange` | Correct sum calculated | **Full** |
| Refund lookup by original ID | `RepositoryQueryTest.TransactionRepositoryTests.findRefundsByOriginalTransactionId` | Refunds found | **Full** |
| Settlement exists by date | `RepositoryQueryTest.SettlementRepositoryTests.existsBySettlementDate` | Existence check correct | **Full** |
| Version ordering | `RepositoryQueryTest.FileRecordRepositoryTests.findByLogicalIdOrderByVersionNumberDesc` | Descending order | **Full** |
| Expired file detection | `RepositoryQueryTest.FileRecordRepositoryTests.findByIsDeletedTrueAndExpirationTimeBefore` | Expired files found | **Full** |
| Audit log by entity | `RepositoryQueryTest.AuditLogRepositoryTests.findByEntityTypeAndEntityId` | Logs found | **Full** |
| **Domain Validation** ||||
| Past start time rejected | `ApiStatusCodeIntegrationTest.BadRequestTests.createAppointmentWithPastTime_returns400` | Status 400 for past time | **Full** |
| Invalid time range rejected | `ApiStatusCodeIntegrationTest.BadRequestTests.createAppointmentWithInvalidTimeRange_returns400` | Status 400 for invalid range | **Full** |
| Missing required fields rejected | `ApiStatusCodeIntegrationTest.BadRequestTests.createPropertyWithMissingFields_returns400` | Status 400 for missing fields | **Full** |
| **Security Components** ||||
| Data masking | `SecureDataMaskerTest.shouldMaskGeneralSensitiveField` | Masking applied correctly | **Full** |
| Secret validation | `SecretValidatorTest.shouldRejectWeakSecret` | Weak secret rejected | **Full** |
| Secondary password verification | `SecondaryVerificationServiceTest.shouldVerifyCorrectSecondaryPassword` | Correct password verified | **Full** |

---

## Testing Verdicts

### Overall Testing Verdict: **PASS**

| Category | Status | Details |
|----------|--------|---------|
| API Status Codes (401/403/404/409) | **PASS** | All status codes covered with full assertions |
| JWT Authentication Filter | **PASS** | Valid, expired, invalid signatures, malformed tokens |
| RBAC Authorization | **PASS** | Role-based access control verified for all roles |
| Object-Level Authorization (IDOR) | **PASS** | Cross-owner access prevention verified |
| Secondary Password Verification | **PASS** | Enforcement on sensitive operations verified |
| Finance - Payment/Refund | **PASS** | Idempotency, validation, conflict handling |
| Finance - Concurrency | **PASS** | Thread-safe idempotent operations |
| File - Soft Delete/Restore | **PASS** | Lifecycle states verified |
| File - Permanent Delete | **PASS** | Role + secondary password enforcement |
| File - Version Management | **PASS** | Version creation and active state |
| File - Access Control | **PASS** | Owner and admin access verified |
| Repository Queries | **PASS** | Query correctness verified |
| Domain Validation | **PASS** | Input validation covered |

---

## Test Commands

### Run All Tests

```bash
# Full test suite (requires infrastructure)
mvn test

# Run with Docker Compose (includes DB, Nacos)
docker compose up -d
mvn test
docker compose down
```

### Run by Category

```bash
# API Status Code Tests
mvn test -Dtest=ApiStatusCodeIntegrationTest

# Security Integration Tests
mvn test -Dtest=AuthSecurityIntegrationTest

# Finance Critical Path Tests
mvn test -Dtest=FinanceCriticalPathTest

# File Critical Path Tests
mvn test -Dtest=FileCriticalPathTest

# Repository Query Tests
mvn test -Dtest=RepositoryQueryTest

# Unit Tests Only (no infrastructure)
mvn test -Dtest="*Test" -pl . -DskipIntegrationTests=true

# Security Component Tests
mvn test -Dtest="SecureDataMaskerTest,SecretValidatorTest,SecondaryVerificationServiceTest"
```

### Run with Coverage Report

```bash
# Generate JaCoCo coverage report
mvn test jacoco:report

# View report (HTML)
open target/site/jacoco/index.html

# Run specific test category with coverage
mvn test -Dtest=ApiStatusCodeIntegrationTest jacoco:report
```

### Docker-based Testing

```bash
# Build and run tests in Docker
docker compose build
docker compose run --rm anju-backend mvn test

# Run specific test class
docker compose run --rm anju-backend mvn test -Dtest=ApiStatusCodeIntegrationTest

# Run with Maven container (no Docker Compose)
docker run --rm -v $(pwd):/app -w /app maven:3.9-eclipse-temurin-17 mvn test
```

### Test Profiles

| Profile | Purpose | Command |
|---------|---------|---------|
| `test` (default) | Standard testing with H2 | `mvn test` |
| `integration` | Full integration tests | `mvn test -Dspring.profiles.active=integration` |
| `ci` | CI-optimized headless tests | `mvn test -Pci` |

### Test Class Reference

| Class | Type | Purpose |
|-------|------|---------|
| `ApiStatusCodeIntegrationTest` | Integration | HTTP status code verification |
| `AuthSecurityIntegrationTest` | Integration | JWT, RBAC, IDOR, secondary password |
| `FinanceCriticalPathTest` | Integration | Payment/refund idempotency, concurrency |
| `FileCriticalPathTest` | Integration | File lifecycle, versions, access |
| `RepositoryQueryTest` | DataJpaTest | Query correctness |
| `SecureDataMaskerTest` | Unit | Data masking utilities |
| `SecretValidatorTest` | Unit | Secret validation |
| `SecondaryVerificationServiceTest` | Unit | Secondary password verification |
| `AppointmentServiceTest` | Unit | Appointment business logic |
| `FinancialServiceTest` | Unit | Financial business logic |
| `FileServiceTest` | Unit | File business logic |
| `SecurityExceptionHandlerTest` | Unit | Exception handling |

---

## Test Environment Requirements

### Local Development
- Java 17+
- Maven 3.9+
- H2 Database (auto-configured)

### Docker Testing
- Docker & Docker Compose
- MySQL 8.0+ (via Docker Compose)
- Nacos 2.2.3+ (via Docker Compose)

### CI/CD
- All profiles work in CI
- Parallel test execution supported
- Coverage reports generated automatically

---

## Known Limitations

1. **Integration Tests**: Require infrastructure (MySQL, Nacos) - use Docker Compose
2. **Concurrency Tests**: Timing-sensitive, may require retry in CI
3. **File Upload Tests**: Physical file storage tests skipped in unit mode
4. **Nacos Dependency**: Some config tests require Nacos to be healthy

---

## Test Maintenance

### Adding New Tests

1. **Unit Tests**: Add to appropriate `*Test.java` class in `src/test/java/com/anju/`
2. **Integration Tests**: Add to `src/test/java/com/anju/controller/` or `src/test/java/com/anju/service/`
3. **Repository Tests**: Add to `RepositoryQueryTest.java`

### Test Naming Convention

```
testMethodName_scenario_expectedResult
```

Example:
```
shouldReturn401ForUnauthenticatedRequest()
handleConcurrentIdempotentRequests()
```
