# Anju Accompanying Medical Appointment Operation Management System

A comprehensive Spring Boot 3.x backend system for managing medical appointments, properties, finances, and file storage with role-based access control.

## Quick Start

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down
```

## Service Addresses

| Service | URL | Credentials |
|---------|-----|-------------|
| **API Backend** | http://localhost:8080 | - |
| **Nacos Dashboard** | http://localhost:8848/nacos | nacos / nacos |
| **MySQL** | localhost:3306 | root / root |
| **API Documentation** | http://localhost:8080/swagger-ui.html | - |

## Project Structure

```
anju-backend/
├── pom.xml                          # Maven configuration
├── src/main/java/com/anju/
│   ├── AnjuBackendApplication.java   # Main application entry
│   ├── config/                      # Configuration classes
│   │   └── SecurityConfig.java
│   ├── controller/                  # REST API controllers
│   │   └── *.java
│   ├── dto/                        # Data Transfer Objects
│   │   └── *.java
│   ├── entity/                     # JPA Entities
│   │   └── *.java
│   ├── exception/                  # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   └── *.java
│   ├── repository/                 # JPA Repositories
│   │   └── *.java
│   ├── security/                  # Security components
│   │   └── *.java
│   └── service/                    # Business logic services
│       └── *.java
└── src/main/resources/
    ├── application.yml             # Application configuration
    └── bootstrap.yml               # Nacos bootstrap config
```

## Running Tests

### Docker Compose (Recommended)

```bash
# Start services first
docker compose up -d

# Or run in existing container
docker compose exec anju-backend mvn test

# Stop services
docker compose down
```

### Unit Tests Only (Fast)

```bash
# Run unit tests only (no integration tests)
docker compose run --rm anju-backend mvn test -Dtest=\
AppointmentServiceTest,\
FinancialServiceTest,\
FileServiceTest,\
PasswordValidatorTest,\
PasswordEncoderTest,\
SecureDataMaskerTest,\
SecretValidatorTest,\
SecondaryVerificationServiceTest
```

### Running Specific Tests

```bash
# Run specific test class
docker compose run --rm anju-backend mvn test -Dtest=AppointmentServiceTest

# Run specific test method
docker compose run --rm anju-backend mvn test -Dtest=ApiResponseTest#shouldCreateSuccessResponseWithData
```

## API Usage Examples

### 1. Login and Get Token

```bash
# Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Anju@1234"
  }'
```

### 2. Create Property

```bash
curl -X POST http://localhost:8080/api/admin/properties \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "uniqueCode": "PROP-001",
    "rent": 5000.00,
    "deposit": 10000.00
  }'
```

### 3. Create Appointment (with Service Type)

```bash
curl -X POST http://localhost:8080/api/admin/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "serviceType": "STANDARD_CONSULTATION",
    "orderAmount": 1500.00,
    "startTime": "2026-04-15T09:00:00",
    "endTime": "2026-04-15T09:30:00",
    "patientName": "John Doe"
  }'
```

### 4. Record Payment

```bash
curl -X POST http://localhost:8080/api/finance/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "idempotencyKey": "payment-12345",
    "appointmentId": 1,
    "amount": 1500.00,
    "channel": "ALIPAY"
  }'
```

## Domain Modules

### 1. Security Module
- JWT-based authentication
- BCrypt password hashing (strength 10)
- Role-based access control (RBAC)
- Password validation (min 8 chars, letters + numbers)
- Field-level encryption-at-rest for sensitive data

**Roles:**
- `ADMIN` - Full system access
- `REVIEWER` - Review and approve properties
- `DISPATCHER` - Manage appointments
- `FINANCE` - Financial operations
- `FRONTLINE` - Property and appointment creation

### 2. Property Module
- Property CRUD operations
- Status workflow: DRAFT → PENDING_REVIEW → LISTED → DELISTED
- Vacancy period management
- Compliance validation workflow

### 3. Appointment Module
- **Service Types with Standard Durations:**
  - `QUICK_CONSULTATION` (15 minutes)
  - `STANDARD_CONSULTATION` (30 minutes)
  - `EXTENDED_CONSULTATION` (60 minutes)
  - `COMPREHENSIVE_REVIEW` (90 minutes)
- Conflict detection (staff and resource)
- 24-hour advance policy:
  - Cancel/reschedule >24 hours: No penalty
  - Cancel/reschedule <24 hours: 10% or 50 RMB penalty
- Max 2 reschedules enforced
- Auto-release stale appointments (15 minutes)

### 4. Financial Module
- Payment recording with idempotency
- Refund processing with validation
- Daily settlement generation
- Bandwidth throttling for file operations

### 5. File Module
- Chunked upload support
- Hash-based deduplication
- Multi-version management
- Recycle bin with 30-day retention
- Real concurrency controls (max 10 concurrent uploads, 50 downloads)
- Bandwidth throttling per operator

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | docker | Spring profile |
| `NACOS_SERVER_ADDR` | nacos | Nacos server address |
| `MYSQL_HOST` | mysql | MySQL host |
| `MYSQL_PORT` | 3306 | MySQL port |
| `MYSQL_DATABASE` | anju_db | Database name |
| `MYSQL_USERNAME` | root | Database username |
| `MYSQL_PASSWORD` | root | Database password |
| `TZ` | Asia/Shanghai | Timezone |

## Health Check

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check all container status
docker compose ps
```

## Troubleshooting

### MySQL Connection Issues
```bash
# Check MySQL logs
docker compose logs mysql

# Verify MySQL is ready
docker exec -it anju-mysql mysql -uroot -proot -e "SELECT 1"
```

### Application Won't Start
```bash
# Check application logs
docker compose logs anju-backend

# Rebuild and restart
docker compose up -d --build
```

## Security Features

### 1. Object-Level Authorization (Anti-IDOR)
- All resource operations validate ownership
- Admins have full access
- Users can only access their own resources

### 2. Secondary Password Verification
Sensitive operations require secondary password verification:
- Invoice issue/reject
- Permanent file deletion
- Refund processing

### 3. Exception Sanitization
- Internal errors return generic messages with error reference
- Stack traces and internal details never exposed
- UUID-based reference IDs for support

### 4. Sensitive Data Masking
- Phone numbers: `138****5678`
- Emails: `te***@example.com`
- IDs: `1101****1234`

### 5. Field-Level Encryption
- AES-256-GCM encryption for sensitive fields
- Configurable encryption key
- Automatic encryption/decryption

### 6. Full-Chain Audit Logging
All sensitive operations logged with:
- Operator ID and username
- Timestamp
- Operation type
- Field changes

## Test Coverage Evidence Matrix

| Requirement | Test Class | Coverage |
|------------|-----------|----------|
| Service Type Validation | AppointmentServiceTest | Full |
| Standard Duration Enforcement (15/30/60/90) | AppointmentServiceTest | Full |
| Conflict Detection | AppointmentServiceTest, RepositoryQueryTest | Full |
| 24-Hour Advance Policy | AppointmentServiceTest | Full |
| Penalty Calculation | AppointmentServiceTest, SecureAppointmentServiceTest | Full |
| Max 2 Reschedules | AppointmentServiceTest | Full |
| Auto-Release (15 min) | AppointmentServiceTest | Full |
| Object Authorization | SecureAppointmentServiceTest | Full |
| Payment Idempotency | IdempotencyServiceTest | Full |
| CSV Import Validation | ImportExportServiceTest | Full |
| CSV Export | ImportExportServiceTest | Full |
| File Throttling | FileThrottleServiceTest | Basic |
| Field Encryption | FieldEncryptionServiceTest | Missing |
| Compliance Workflow | PropertyRepositoryTest | Missing |
| Vacancy Management | VacancyPeriodRepositoryTest | Missing |

## Import/Export Features

### CSV Import Validation
- Required field validation (service_type, start_time, end_time, patient_name)
- Enum validation (service types)
- Duration validation (15/30/60/90 minutes)
- Date format validation (ISO and standard formats)
- Length validation
- Error reporting with line numbers

### CSV Export
- UTF-8 encoding
- Proper escaping of special characters
- All appointment fields included

## License

MIT License
