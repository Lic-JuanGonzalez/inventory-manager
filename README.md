# Sistema de Gestión de Inventario Multi-Sucursal

Sistema empresarial para administrar inventario distribuido entre múltiples sucursales, con control de stock, movimientos, transferencias y auditoría completa.

---

## Stack Tecnológico

| Capa          | Tecnología                                        |
|---------------|---------------------------------------------------|
| Backend       | Java 21 · Spring Boot 3.2 · Spring Security · JWT |
| Persistencia  | PostgreSQL 16 · Flyway · Spring Data JPA           |
| Frontend      | React 18 · Vite · Material UI · React Router       |
| Infraestructura | Docker · Docker Compose                          |
| Testing       | JUnit 5 · Mockito · Testcontainers                 |
| Docs API      | Swagger / OpenAPI 3                                |

---

## Arquitectura

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

## Estructura del Proyecto

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

## Modelo Entidad-Relación

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

## Roles y Permisos

| Acción                        | ADMIN | OPERATOR | AUDITOR |
|-------------------------------|:-----:|:--------:|:-------:|
| CRUD Usuarios                 |  ✓    |          |         |
| CRUD Sucursales               |  ✓    |          |         |
| CRUD Productos                |  ✓    |          |         |
| Ver Productos/Sucursales      |  ✓    |    ✓     |    ✓    |
| Ver Inventario                |  ✓    |    ✓     |    ✓    |
| Registrar Movimientos         |  ✓    |    ✓     |         |
| Solicitar Transferencias      |  ✓    |    ✓     |         |
| Aprobar Transferencias        |  ✓    |          |         |
| Ver Reportes / Auditoría      |  ✓    |          |    ✓    |
| Ver Dashboard                 |  ✓    |    ✓     |    ✓    |

---

## Flujo de Transferencias

```
OPERADOR                  ADMIN                OPERADOR (ORIGEN)  OPERADOR (DESTINO)
    │                       │                          │                   │
    │── Crear (PENDIENTE)──►│                          │                   │
    │                       │── Aprobar (APROBADA) ──► │                   │
    │                       │                          │── Enviar ────────►│
    │                       │                          │  (EN_TRANSITO)    │
    │                       │                          │                   │── Recibir (RECIBIDA)
    │                       │                          │                   │
    │◄── En cualquier estado PENDIENTE/APROBADA ──  CANCELAR ──────────────┘
```

---

## API REST — Endpoints Principales

### Autenticación
```
POST   /api/auth/login          Iniciar sesión
POST   /api/auth/refresh        Renovar access token
POST   /api/auth/logout         Cerrar sesión
```

### Productos
```
GET    /api/products            Listar (filtros: search, categoryId, active, page, size)
GET    /api/products/{id}       Obtener por ID
POST   /api/products            Crear [ADMIN]
PUT    /api/products/{id}       Actualizar [ADMIN]
PATCH  /api/products/{id}/toggle-active  Activar/desactivar [ADMIN]
```

### Sucursales
```
GET    /api/branches            Listar
GET    /api/branches/active     Solo activas
POST   /api/branches            Crear [ADMIN]
PUT    /api/branches/{id}       Actualizar [ADMIN]
PATCH  /api/branches/{id}/toggle-active
```

### Inventario
```
GET    /api/inventory           Listar (filtros: branchId, productId, categoryId)
GET    /api/inventory/low-stock Productos bajo stock mínimo
POST   /api/inventory/initialize  Inicializar inventario en sucursal [ADMIN/OPERATOR]
POST   /api/inventory/movement  Registrar entrada/salida
GET    /api/inventory/movements Historial de movimientos
```

### Transferencias
```
GET    /api/transfers           Listar
GET    /api/transfers/pending   Pendientes
POST   /api/transfers           Solicitar
PUT    /api/transfers/{id}/approve   Aprobar [ADMIN]
PUT    /api/transfers/{id}/ship      Enviar
PUT    /api/transfers/{id}/receive   Recibir
PUT    /api/transfers/{id}/cancel    Cancelar
```

### Dashboard
```
GET    /api/dashboard           Métricas e indicadores
```

### Auditoría
```
GET    /api/audit               Logs de auditoría [ADMIN/AUDITOR]
```

---

## Inicio Rápido

### Opción 1: Docker Compose (recomendado)

```bash
# Clonar y configurar variables
cp .env.example .env

# Levantar todos los servicios
docker compose up --build

# Ver logs
docker compose logs -f backend

# Frontend disponible en: http://localhost
# Backend API:           http://localhost:8080/api
# Swagger UI:            http://localhost:8080/api/swagger-ui.html
```

### Opción 2: Desarrollo local

**Prerrequisitos:** Java 21+, Maven 3.9+, Node 20+, PostgreSQL 16

```bash
# Base de datos
createdb inventory_db
createuser inventory_user

# Backend
cd backend
export DB_HOST=localhost DB_USER=inventory_user DB_PASSWORD=inventory_pass
mvn spring-boot:run

# Frontend (nueva terminal)
cd frontend
npm install
npm run dev
# Disponible en http://localhost:5173
```

---

## Credenciales Demo

| Rol       | Email                    | Contraseña  |
|-----------|--------------------------|-------------|
| Admin     | admin@inventory.com      | Admin@1234  |

---

## Tests

```bash
cd backend

# Ejecutar todos los tests
mvn test

# Con reporte de cobertura (JaCoCo)
mvn verify

# Reporte en: target/site/jacoco/index.html
```

---

## Reglas de Negocio

1. **Stock nunca negativo** — validado a nivel de entidad y servicio
2. **SKU único** — validado con constraint UNIQUE en DB y a nivel servicio
3. **Transferencias verifican stock disponible** antes de crear solicitud y antes de enviar
4. **Movimientos históricos inmutables** — no se modifica ni elimina historial
5. **Toda operación genera auditoría asíncrona** — no bloquea la transacción principal
6. **Refresh token rotante** — cada refresh invalida el anterior
7. **Alertas de bajo stock** — flag `belowMinStock` calculado en tiempo real

---

## Roadmap de Mejoras Futuras

- [ ] Notificaciones en tiempo real (WebSocket/SSE) para alertas de stock
- [ ] Exportación de reportes a PDF/Excel
- [ ] Código de barras / QR por producto
- [ ] Multi-tenant (múltiples empresas)
- [ ] Integración con proveedores (órdenes de compra automáticas)
- [ ] App móvil (React Native o Flutter)
- [ ] Caché con Redis para reportes frecuentes
- [ ] Métricas Prometheus + Grafana
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Tests E2E con Playwright
- [ ] Soporte para lotes y fechas de vencimiento
- [ ] Historial de precios de referencia

---

## Diagrama de Secuencia — Transferencia Exitosa

```
Operador  Frontend  BackendAPI  TransferService  InventoryService  DB
   │          │          │               │                │          │
   │─ POST /transfers ──►│               │                │          │
   │          │──────────►───────────────►                │          │
   │          │          │  verificar stock origen        │          │
   │          │          │──────────────────────────────► │          │
   │          │          │◄── stock OK ──────────────────-│          │
   │          │          │  save(PENDIENTE) ──────────────────────── ►│
   │◄ 201 ───-│◄─────────│◄──────────────│                │          │
   │          │          │               │                │          │
   │─ PUT /approve ──────►───────────────►  update APROBADA ─────── ►│
   │◄ 200 ───-│◄─────────│◄──────────────│                │          │
   │          │          │               │                │          │
   │─ PUT /ship ─────────►──────────────►descuentoStock ─►│          │
   │          │          │              createMovimiento ─►│ ──────── ►│
   │◄ 200 ───-│◄─────────│◄──────────────│                │          │
   │          │          │               │                │          │
   │─ PUT /receive ──────►──────────────►addStock destino►│          │
   │          │          │              createMovimiento ─►│ ──────── ►│
   │◄ 200 ───-│◄─────────│◄──────────────│                │          │
```
