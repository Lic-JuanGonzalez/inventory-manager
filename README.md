# Multi-Branch Inventory Management System

Enterprise system for managing distributed inventory across multiple branches, with stock control, movements, transfers, and full audit trail.

---

## Tech Stack

| Layer          | Technology                                        |
|----------------|---------------------------------------------------|
| Backend        | Java 21 · Spring Boot 3.2 · Spring Security · JWT |
| Persistence    | PostgreSQL 16 · Flyway · Spring Data JPA           |
| Frontend       | React 18 · Vite · Material UI · React Router       |
| Infrastructure | Docker · Docker Compose                            |
| Testing        | JUnit 5 · Mockito · Testcontainers                 |
| API Docs       | Swagger / OpenAPI 3                                |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Frontend (React)                    │
│  LoginPage │ Dashboard │ Products │ Inventory │ Transfers │
│                 Axios + JWT Interceptors                  │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP / REST
┌──────────────────────────▼──────────────────────────────┐
│                  Backend (Spring Boot)                    │
│  Controllers → Services → Repositories → Entities        │
│  JwtAuthFilter → SecurityConfig → GlobalExceptionHandler │
└──────────────────────────┬──────────────────────────────┘
                           │ JPA / Flyway
┌──────────────────────────▼──────────────────────────────┐
│                  PostgreSQL 16                            │
│  users · branches · products · inventory                 │
│  inventory_movements · transfer_requests · audit_logs    │
└─────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
multi-branch-directory/
├── backend/
│   ├── src/main/java/com/inventory/management/
│   │   ├── config/           # Security, Swagger, CORS
│   │   ├── controller/       # REST Controllers
│   │   ├── domain/
│   │   │   ├── entity/       # JPA Entities
│   │   │   ├── enums/        # RoleType, MovementType, etc.
│   │   │   └── repository/   # Spring Data Repositories
│   │   ├── dto/
│   │   │   ├── request/      # Input DTOs (records)
│   │   │   └── response/     # Output DTOs (records)
│   │   ├── exception/        # GlobalExceptionHandler, custom exceptions
│   │   ├── security/         # JWT, UserDetails, Filter
│   │   └── service/          # Business logic
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/     # Flyway scripts
│   └── src/test/             # Unit + Integration tests
├── frontend/
│   └── src/
│       ├── api/              # Axios API clients
│       ├── components/       # Layout, Navbar, Sidebar
│       ├── context/          # AuthContext
│       └── pages/            # Feature pages + dialogs
├── docker-compose.yml
└── .env.example
```

---

## Entity-Relationship Model

```
roles ──< users >─────────── refresh_tokens
           │
           ├── audit_logs
           ├── inventory_movements
           └── transfer_requests

product_categories ──< products >── inventory
                                       │
branches ──────────────────────────────┘
    │
    ├── inventory_movements
    └── transfer_requests (origin + destination)
```

---

## Roles and Permissions

| Action                        | ADMIN | OPERATOR | AUDITOR |
|-------------------------------|:-----:|:--------:|:-------:|
| CRUD Users                    |  ✓    |          |         |
| CRUD Branches                 |  ✓    |          |         |
| CRUD Products                 |  ✓    |          |         |
| View Products/Branches        |  ✓    |    ✓     |    ✓    |
| View Inventory                |  ✓    |    ✓     |    ✓    |
| Register Movements            |  ✓    |    ✓     |         |
| Request Transfers             |  ✓    |    ✓     |         |
| Approve Transfers             |  ✓    |          |         |
| View Reports / Audit          |  ✓    |          |    ✓    |
| View Dashboard                |  ✓    |    ✓     |    ✓    |

---

## Transfer Workflow

```
OPERATOR               ADMIN           OPERATOR (ORIGIN)  OPERATOR (DESTINATION)
    │                    │                     │                    │
    │── Create (PENDING)►│                     │                    │
    │                    │── Approve ─────────►│                    │
    │                    │  (APPROVED)          │                    │
    │                    │                     │── Ship ───────────►│
    │                    │                     │  (IN_TRANSIT)      │
    │                    │                     │                    │── Receive (RECEIVED)
    │                    │                     │                    │
    │◄── CANCEL from any PENDING/APPROVED state ───────────────────┘
```

---

## REST API — Main Endpoints

### Authentication
```
POST   /api/auth/login          Login
POST   /api/auth/refresh        Renew access token
POST   /api/auth/logout         Logout
```

### Products
```
GET    /api/products            List (filters: search, categoryId, active, page, size)
GET    /api/products/{id}       Get by ID
POST   /api/products            Create [ADMIN]
PUT    /api/products/{id}       Update [ADMIN]
PATCH  /api/products/{id}/toggle-active  Activate/deactivate [ADMIN]
```

### Branches
```
GET    /api/branches            List
GET    /api/branches/active     Active only
POST   /api/branches            Create [ADMIN]
PUT    /api/branches/{id}       Update [ADMIN]
PATCH  /api/branches/{id}/toggle-active
```

### Inventory
```
GET    /api/inventory           List (filters: branchId, productId, categoryId)
GET    /api/inventory/low-stock Products below minimum stock
POST   /api/inventory/initialize  Initialize inventory in branch [ADMIN/OPERATOR]
POST   /api/inventory/movement  Register inbound/outbound movement
GET    /api/inventory/movements Movement history
```

### Transfers
```
GET    /api/transfers           List
GET    /api/transfers/pending   Pending transfers
POST   /api/transfers           Request transfer
PUT    /api/transfers/{id}/approve   Approve [ADMIN]
PUT    /api/transfers/{id}/ship      Ship
PUT    /api/transfers/{id}/receive   Receive
PUT    /api/transfers/{id}/cancel    Cancel
```

### Dashboard
```
GET    /api/dashboard           Metrics and KPIs
```

### Audit
```
GET    /api/audit               Audit logs [ADMIN/AUDITOR]
```

---

## Quick Start

### Prerequisites

| Option | Requirements |
|--------|-------------|
| Docker (recommended) | [Docker](https://docs.docker.com/get-docker/) + [Docker Compose](https://docs.docker.com/compose/install/) |
| Local | Java 21+, Maven 3.9+, Node 20+, PostgreSQL 16 |

### Option 1: Docker Compose (recommended)

```bash
# 1. Clone
git clone https://github.com/Lic-JuanGonzalez/inventory-manager.git
cd inventory-manager

# 2. Configure environment
cp .env.example .env

# 3. Build and start all services
docker compose up --build

# Frontend:  http://localhost
# Backend:   http://localhost:8080/api
# Swagger:   http://localhost:8080/api/swagger-ui.html
```

**Default login:** `admin@inventory.com` / `Admin@1234`

### Option 2: Local Development

```bash
# Database (PostgreSQL must be running)
createdb inventory_db

# Backend
cd backend
export DB_HOST=localhost DB_USER=postgres DB_PASSWORD=postgres
mvn spring-boot:run
# API at http://localhost:8080/api

# Frontend (new terminal)
cd frontend
npm install
npm run dev
# UI at http://localhost:5173
```

**Default login:** `admin@inventory.com` / `Admin@1234`

---

## Demo Credentials

| Role      | Email                    | Password    |
|-----------|--------------------------|-------------|
| Admin     | admin@inventory.com      | Admin@1234  |

---

## API Testing with Apidog / Newman

Collection at `docs/api/inventory-api-collection.json` — 76 requests, 118 automated assertions (Postman v2.1, importable by Apidog).

### Import into Apidog

1. **Import** → **Postman** → select `docs/api/inventory-api-collection.json`
2. Set `baseUrl` environment variable to `http://localhost:8080/api`
3. Click **Run** → **Run All**

### Run via Newman (no global install)

Requires clean database — test data creates unique emails, SKUs, and branch names:

```bash
# Reset DB and start services
docker compose down -v && docker compose up -d postgres backend

# Run (npx downloads Newman on demand)
npx newman run docs/api/inventory-api-collection.json \
  --env-var baseUrl=http://localhost:8080/api

# Expected: 76 requests, 118 assertions, 0 failures
```

---

## Unit Tests

```bash
cd backend

# Run all tests
mvn test

# With coverage report (JaCoCo)
mvn verify

# Report at: target/site/jacoco/index.html
```

---

## Business Rules

1. **Stock never negative** — validated at entity and service level
2. **Unique SKU** — enforced via UNIQUE DB constraint and service layer
3. **Transfers verify available stock** before creating request and before shipping
4. **Movement history is immutable** — no modification or deletion of historical records
5. **Every operation generates async audit** — does not block the main transaction
6. **Rotating refresh token** — each refresh invalidates the previous one
7. **Low-stock alerts** — `belowMinStock` flag calculated in real time

---

## Future Roadmap

- [ ] Real-time notifications (WebSocket/SSE) for stock alerts
- [ ] Report export to PDF/Excel
- [ ] Barcode / QR code per product
- [ ] Multi-tenant support (multiple companies)
- [ ] Supplier integration (automatic purchase orders)
- [ ] Mobile app (React Native or Flutter)
- [ ] Redis cache for frequent reports
- [ ] Prometheus + Grafana metrics
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] E2E tests with Playwright
- [ ] Batch and expiry date support
- [ ] Reference price history

---

## Sequence Diagram — Successful Transfer

```
Operator  Frontend  BackendAPI  TransferService  InventoryService  DB
   │          │          │               │                │          │
   │─ POST /transfers ──►│               │                │          │
   │          │──────────►───────────────►                │          │
   │          │          │  check origin stock            │          │
   │          │          │──────────────────────────────► │          │
   │          │          │◄── stock OK ──────────────────-│          │
   │          │          │  save(PENDING) ────────────────────────── ►│
   │◄ 201 ───-│◄─────────│◄──────────────│                │          │
   │          │          │               │                │          │
   │─ PUT /approve ──────►───────────────►  update APPROVED ──────── ►│
   │◄ 200 ───-│◄─────────│◄──────────────│                │          │
   │          │          │               │                │          │
   │─ PUT /ship ─────────►──────────────►deductStock ────►│          │
   │          │          │              createMovement ───►│ ──────── ►│
   │◄ 200 ───-│◄─────────│◄──────────────│                │          │
   │          │          │               │                │          │
   │─ PUT /receive ──────►──────────────►addStock dest ──►│          │
   │          │          │              createMovement ───►│ ──────── ►│
   │◄ 200 ───-│◄─────────│◄──────────────│                │          │
```
