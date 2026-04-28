# Sclera 2.0 AMP – Microservices Architecture

## Overview

The monolithic Device domain is split into **6 independent Spring Boot microservices**, all sharing
the same MySQL database (shared-DB strategy) but with clear ownership boundaries per service.

```
                         ┌────────────────────────────┐
                         │      discovery-service      │
                         │      (Eureka Registry)      │
                         │         Port 8761           │
                         └────────────┬───────────────┘
                                      │  all services register here
┌─────────────────────────────────────▼──────────────────────────────────────┐
│                           api-gateway  (Port 8080)                         │
│          Spring Cloud Gateway – routes + Eureka load-balanced URIs         │
└──────┬────────────────┬──────────────────┬─────────────────────┬───────────┘
       │                │                  │                     │
  /api/devices/**  /api/onboard/**   /api/assets/**         /api/ai/**
       │                │                  │                     │
  Port 8085        Port 8082          Port 8083             Port 8084
┌──────▼──────┐ ┌───────▼──────┐ ┌────────▼──────┐ ┌──────────▼──────┐
│asset-service│ │asset-onboard-│ │asset-onboarding│ │asset-onboarding-│
│             │ │   service    │ │   -service     │ │   ai-service    │
│Owns: device │ │Owns:         │ │Owns:           │ │No DB tables     │
│             │ │device_onboard│ │asset           │ │(stateless)      │
│             │ │_status       │ │asset_device_   │ │                 │
│             │ │device_onboard│ │mapping         │ │                 │
│             │ │_status_      │ │asset_field     │ │                 │
│             │ │assignee      │ │                │ │                 │
└─────────────┘ └──────────────┘ └────────────────┘ └─────────────────┘
       │                │                  │                     │
       └────────────────┴──────────────────┴─────────────────────┘
                                   │
                           MySQL (shared DB)
                           database: vdms
```

## Service Responsibilities

### 1. discovery-service (Port 8761)
- **Domain:** Service Registry
- **Owns:** No DB tables
- **Responsibility:** Eureka server — all services register here; enables load-balanced `lb://` URIs
- **Must start first** before any other service

### 2. api-gateway (Port 8080)
- **Domain:** Edge / Routing
- **Owns:** No DB tables
- **Responsibility:** Single entry point; routes requests to downstream services via Eureka discovery
- **Route table:**

| Route ID | Predicate | Target |
|---|---|---|
| asset-service | `/api/devices/**` | `lb://asset-service` |
| asset-onboard-service | `/api/onboard/**` | `lb://asset-onboard-service` |
| asset-onboarding-service | `/api/assets/**` | `lb://asset-onboarding-service` |
| asset-onboarding-ai-service | `/api/ai/**` | `lb://asset-onboarding-ai-service` |

### 3. asset-service (Port 8085)
- **Domain:** Device / Managed Asset
- **Owns:** `device` table
- **Responsibility:** Full CRUD for Device entities; onboard-status and asset-match-status patch endpoints
- **Callers:** asset-onboard-service, asset-onboarding-service, asset-onboarding-ai-service

### 4. asset-onboard-service (Port 8082)
- **Domain:** Asset Onboard Workflow
- **Owns:** `device_onboard_status`, `device_onboard_status_assignee`
- **Responsibility:** Manage the image/geo/tag/field onboarding steps per device
- **Calls:** asset-service (verify device, patch onboard_status)

### 5. asset-onboarding-service (Port 8083)
- **Domain:** Asset Onboarding (import + mapping)
- **Owns:** `asset`, `asset_device_mapping`, `asset_field`
- **Responsibility:** Asset import (Corrigo, CSV, manual), asset-device matching, field config
- **Calls:** asset-service (verify device, update asset_match_status)

### 6. asset-onboarding-ai-service (Port 8084)
- **Domain:** AI-driven Matching
- **Owns:** No tables (stateless orchestrator)
- **Responsibility:** Score-based candidate matching; auto-mapping above threshold
- **Calls:** asset-service (get devices), asset-onboarding-service (get assets, create mapping)

---

## Startup Order

```
1. discovery-service   (8761)  — Eureka must be up first
2. api-gateway         (8080)  — needs Eureka to resolve lb:// routes
3. asset-service       (8085)  — core dependency for all other services
4. asset-onboard-service      (8082)  ─┐
5. asset-onboarding-service   (8083)  ─┤  can start in any order after asset-service
6. asset-onboarding-ai-service(8084)  ─┘
```

---

## Entity Refactoring – Floating References

All `@ManyToOne` / `@OneToMany` ORM relationships converted to plain ID columns:

| Entity | Was (ORM) | Now (ID field) |
|--------|-----------|----------------|
| Device | `@ManyToOne Docker docker` | `String docker_id` |
| Device | `@ManyToOne Location location` | `String location_id` |
| Device | `@ManyToOne Product_Details` | `String product_id` |
| Device | `@ManyToOne Phonebook global_vendor` | `String global_vendor_id` |
| Device | `@OneToMany Set<Interface>` | Queried via interface-service |
| DeviceOnboardStatus | `@OneToOne Device device` | `String device_id` |
| DeviceOnboardStatusAssignee | `@ManyToOne DeviceOnboardStatus` | `String device_onboard_status_id` |
| Asset | `@ManyToOne Vdms vdms` | `String vdms_id` |
| AssetDeviceMapping | `@ManyToOne Device device` | `String device_id` |
| AssetDeviceMapping | `@ManyToOne Asset asset` | `String asset_id` |

---

## API Contracts

### api-gateway (Port 8080) — all client traffic enters here
All routes below are accessible via `http://localhost:8080`. The gateway proxies to the correct downstream service transparently.

### asset-service (Port 8085)
| Method | Path | Purpose |
|--------|------|---------|
| GET    | /api/devices/{id}?vdmsId= | Get device |
| GET    | /api/devices?vdmsId=&dockerId=&search=&page=&size= | Filtered list |
| POST   | /api/devices?username= | Create device |
| PUT    | /api/devices/{id}?username= | Full update |
| PATCH  | /api/devices/{id}/onboard-status | Patch onboard_status |
| PATCH  | /api/devices/{id}/asset-match-status | Patch asset_match_status |
| DELETE | /api/devices/{id} | Delete device |
| GET    | /api/devices/onboard-count?dockerId= | Counts by status |

### asset-onboard-service (Port 8082)
| Method | Path | Purpose |
|--------|------|---------|
| POST   | /api/onboard/assets/upsert?vdmsId= | Upsert onboard records |
| PUT    | /api/onboard/devices/{id}/data?vdmsId=&onboardStatus= | Update image/geo/tag/field |
| GET    | /api/onboard/devices/{id}/status?vdmsId= | Get onboard status |
| GET    | /api/onboard/assignees?vdmsId= | List assignee emails |

### asset-onboarding-service (Port 8083)
| Method | Path | Purpose |
|--------|------|---------|
| GET    | /api/assets?vdmsId=&importType=&search=&page=&size= | List assets |
| GET    | /api/assets/unmapped?vdmsId= | Unmapped assets |
| POST   | /api/assets | Upsert asset |
| DELETE | /api/assets/{id} | Delete asset + mappings |
| POST   | /api/assets/mappings?vdmsId= | Create mapping |
| DELETE | /api/assets/mappings/{id}?vdmsId= | Delete mapping |
| GET    | /api/assets/mappings?deviceId= | Mappings for device |
| GET    | /api/assets/fields | Asset field definitions |

### asset-onboarding-ai-service (Port 8084)
| Method | Path | Purpose |
|--------|------|---------|
| POST   | /api/ai/suggest | Ranked suggestions for device |
| POST   | /api/ai/batch-match?vdmsId= | Auto-match all AI-enabled devices |
