# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-06-05

### Fixed

#### Backend
- `GET /inventory/movements` returned 500 on all calls: replaced JPQL `@Query` with `JpaSpecificationExecutor` + `InventoryMovementSpecs` (Criteria API) to avoid Hibernate 6 / PostgreSQL null parameter type inference failure on enum and `Instant` filter params
- `GET /transfers` returned 500 on all calls: same fix via `TransferRequestSpecs`
- `GET /audit` returned 500 on all calls: same fix via `AuditLogSpecs`
- Invalid `MovementReason` enum value in request body returned 500: added `HttpMessageNotReadableException` handler in `GlobalExceptionHandler` → now returns 400

### Added

#### Docs
- Postman Collection v2.1 (`docs/api/inventory-api-collection.json`): 62 requests across 9 folders with automated test scripts, importable by Apidog
  - Folders: Auth, Dashboard, Users, Products, Branches, Inventory, Transfers, Audit, Security
  - Bearer token auth on all authenticated requests; `noauth` on login/refresh/logout
  - `pm.environment.set()` for inter-step variable propagation (`accessToken`, `refreshToken`, `operatorToken`, `newUserId`, `newProductId`, `newBranchId`, `transferId`, `cancelTransferId`)

## [1.0.0] - 2026-05-31

### Added

#### Infrastructure
- Docker Compose setup with PostgreSQL 16, Spring Boot backend and Nginx frontend
- Multi-stage Dockerfiles for backend (Maven + JRE) and frontend (Node + Nginx)
- Environment variable configuration via `.env.example`
- Bridge network `inventory_net` with healthchecks on all services

#### Backend — Java 21 / Spring Boot 3.2
- Stateless JWT authentication with access tokens (15 min) and rotating refresh tokens (7 days)
- Role-based access control: `ADMIN`, `OPERATOR`, `AUDITOR`
- Domain entities: `User`, `Branch`, `Product`, `ProductCategory`, `Inventory`, `InventoryMovement`, `TransferRequest`, `AuditLog`, `RefreshToken`, `Role`
- Custom PostgreSQL ENUMs: `movement_type`, `movement_reason`, `transfer_status`
- Flyway migrations: V1 schema creation, V2 seed data (3 roles, 5 branches, 5 products, admin user)
- HikariCP connection pool (max 20)
- REST API endpoints: `/api/auth`, `/api/users`, `/api/branches`, `/api/products`, `/api/inventory`, `/api/transfers`, `/api/audit`, `/api/dashboard`
- Inter-branch transfer workflow with 5-state lifecycle: `PENDING → APPROVED/REJECTED → IN_TRANSIT → COMPLETED`
- Inventory movement tracking with typed reasons (`PURCHASE`, `SALE`, `POSITIVE_ADJUSTMENT`, `NEGATIVE_ADJUSTMENT`, `TRANSFER_INBOUND`, `TRANSFER_OUTBOUND`, `RETURN_INBOUND`, `LOSS`)
- Async audit logging via `@EnableAsync`
- RFC 7807 ProblemDetail error responses
- Springdoc OpenAPI (Swagger UI) at `/api/swagger-ui.html`
- Spring Boot Actuator health endpoint
- JaCoCo coverage configuration (80% line target)

#### Frontend — React 18 / Vite
- JWT auth context with localStorage token management and automatic refresh rotation
- Axios interceptor with failed-request queue for transparent token refresh
- Role-filtered navigation sidebar
- Pages: Login, Dashboard (Recharts charts), Products, Branches, Inventory, Transfers, Movements, Audit Logs, Users
- Material UI v5 component library with MUI X DataGrid
- Nginx reverse proxy config routing `/api` to backend

### Default Credentials
- **Admin:** `admin@inventory.com` / `Admin@1234`
