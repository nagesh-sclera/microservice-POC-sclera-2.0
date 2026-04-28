# Sclera 2.0 AMP — Asset Management Platform

A Spring Boot microservices application for device and asset onboarding management.

---

## Services Overview

| Service | Port | Description |
|---|---|---|
| `discovery-service` | 8761 | Eureka service registry |
| `api-gateway` | 8080 | Single entry point, circuit breaker |
| `asset-service` | 8085 | Core device management |
| `asset-onboard-service` | 8082 | Onboard workflow and assignees |
| `asset-onboarding-service` | 8083 | Asset import (Corrigo, CSV, manual) |
| `asset-onboarding-ai-service` | 8084 | AI-driven device-asset matching |

---

## Prerequisites

- Java 11
- Maven 3.6+
- MySQL 8.x running on `localhost:3306`

---

## Database Setup

All services share a single MySQL database named `vdms`.

```sql
CREATE DATABASE vdms;
```

Default credentials (development only):

```
Username: root
Password: mypass123
```

Hibernate is configured with `ddl-auto: update` — tables are created automatically on first startup.

---

## Build

Build all services from the root directory:

```bash
mvn clean package -DskipTests
```

Or build a single service:

```bash
mvn clean package -pl asset-service -am -DskipTests
```

---

## Running the Services

Services must be started in the following order:

### 1. Discovery Service (start first)

```bash
java -jar discovery-service/target/discovery-service-1.0.0.jar
```

Eureka dashboard: http://localhost:8761

### 2. API Gateway

```bash
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

### 3. Asset Service (core — start before the others)

```bash
java -jar asset-service/target/asset-service-1.0.0.jar
```

### 4. Remaining Services (can start in any order)

```bash
java -jar asset-onboard-service/target/asset-onboard-service-1.0.0.jar
java -jar asset-onboarding-service/target/asset-onboarding-service-1.0.0.jar
java -jar asset-onboarding-ai-service/target/asset-onboarding-ai-service-1.0.0.jar
```

---

## API Routes (via Gateway — base URL: `http://localhost:8080`)

| Prefix | Routed To |
|---|---|
| `/api/devices/**` | asset-service |
| `/api/onboard/**` | asset-onboard-service |
| `/api/assets/**` | asset-onboarding-service |
| `/api/ai/**` | asset-onboarding-ai-service |

---

## Architecture

See the [`architecture/`](architecture/) folder for detailed documentation:

- `ARCHITECTURE.md` — Service responsibilities and API contracts
- `SEQUENCE_DIAGRAMS.md` — Request flow diagrams
- `MIGRATION_PLAN.md` — Migration strategy
- `RISKS_AND_MITIGATIONS.md` — Risk assessment