# Sclera 2.0 AMP – Microservices Architecture

## Overview

The monolithic Device domain is split into **8 independent Spring Boot microservices**, all sharing
the same MySQL database (shared-DB strategy) but with clear ownership boundaries per service.
The API Gateway adds circuit-breaker + retry resilience on every route.

```
                         ┌────────────────────────────┐
                         │      discovery-service      │
                         │      (Eureka Registry)      │
                         │         Port 8761           │
                         └────────────┬───────────────┘
                                      │  all services register here
┌─────────────────────────────────────▼──────────────────────────────────────────────────────┐
│                              api-gateway  (Port 8080)                                      │
│       Spring Cloud Gateway – routes + Eureka lb:// + Retry(3) + CircuitBreaker             │
└──┬──────────────┬───────────────┬──────────────┬────────────────┬──────────────────────────┘
   │              │               │              │                │               │
/api/devices/** /api/onboard/**  /api/assets/**  /api/ai/**  /api/device-     /api/device-
   │              │               │              │            lifecycle/**      specification/**
   │              │               │              │                │               │
 :8085          :8082           :8083          :8084          :dynamic         :dynamic
┌──▼──────┐  ┌───▼──────────┐  ┌─▼────────────┐  ┌──▼────────────┐  ┌──▼──────────────┐  ┌──▼──────────────────┐
│ asset-  │  │asset-onboard-│  │asset-onboarding│ │asset-onboarding│  │device-lifecycle │  │device-specification │
│ service │  │  service     │  │  -service     │  │  -ai-service   │  │   -service      │  │   -service          │
│         │  │              │  │               │  │                │  │                 │  │                     │
│ device  │  │device_onboard│  │asset          │  │No DB (stateless│  │device_lifecycle │  │device_specification │
│ table   │  │_status       │  │asset_device_  │  │orchestrator)   │  │_history         │  │device_network_spec  │
│         │  │device_onboard│  │mapping        │  │                │  │                 │  │device_installed_apps│
│         │  │_status_      │  │asset_field    │  │                │  │                 │  │                     │
│         │  │assignee      │  │               │  │                │  │                 │  │                     │
└─────────┘  └──────────────┘  └───────────────┘  └────────────────┘  └─────────────────┘  └─────────────────────┘
     │              │                  │                  │                   │                        │
     └──────────────┴──────────────────┴──────────────────┴───────────────────┴────────────────────────┘
                                                │
                                        MySQL (shared DB)
                                        database: vdms
```

---

## Service Responsibilities

### 1. discovery-service (Port 8761)
- **Domain:** Service Registry
- **Owns:** No DB tables
- **Responsibility:** Eureka server — all services register here; enables load-balanced `lb://` URIs
- **Must start first** before any other service

### 2. api-gateway (Port 8080)
- **Domain:** Edge / Routing / Resilience
- **Owns:** No DB tables
- **Responsibility:** Single entry point; routes requests to downstream services via Eureka discovery.
  Every route has **Retry (3 attempts, exponential backoff)** and **CircuitBreaker (Resilience4j)**
- **Route table:**

| Route ID | Predicate | Target | Circuit Breaker |
|---|---|---|---|
| asset-service | `/api/devices/**` | `lb://asset-service` | assetServiceCB |
| asset-onboard-service | `/api/onboard/**` | `lb://asset-onboard-service` | assetOnboardServiceCB |
| asset-onboarding-service | `/api/assets/**` | `lb://asset-onboarding-service` | assetOnboardingServiceCB |
| asset-onboarding-ai-service | `/api/ai/**` | `lb://asset-onboarding-ai-service` | assetOnboardingAiServiceCB |
| device-lifecycle-service | `/api/device-lifecycle/**` | `lb://device-lifecycle-service` | deviceLifecycleServiceCB |
| device-specification-service | `/api/device-specification/**` | `lb://device-specification-service` | deviceSpecificationServiceCB |

### 3. asset-service (Port 8085)
- **Domain:** Device / Managed Asset
- **Owns:** `device` table
- **Responsibility:** Full CRUD for Device entities; onboard-status and asset-match-status patch endpoints
- **Callers:** asset-onboard-service, asset-onboarding-service, asset-onboarding-ai-service, device-lifecycle-service, device-specification-service

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

### 7. device-lifecycle-service (Port: dynamic — registered in Eureka)
- **Domain:** Device Lifecycle & Operational Status
- **Owns:** `device_lifecycle_history`
- **Responsibility:** Records every assignment, unassignment, and retire event per device.
  Syncs operational status changes back to asset-service. Triggers full retire flow via inventory-service.
- **Calls:**
  - asset-service: PATCH operational-status, PATCH assigned-user-email, GET device, POST archive-devices
  - inventory-service: POST retire-inventory-device

### 8. device-specification-service (Port: dynamic — registered in Eureka)
- **Domain:** Device Hardware & Software Specification
- **Owns:** `device_specification`, `device_network_specification`, `device_installed_apps`
- **Responsibility:** Ingests full or delta spec payloads from the device agent.
  Upserts hardware, OS, network, and installed-app data. Syncs device fields and online status back to asset-service.
  Creates virtual/child devices in asset-service. Notifies inventory-service of unknown devices.
- **Calls:**
  - asset-service: GET by serial-number, PATCH device fields, PATCH online-status, GET by display-name+parent, POST virtual-device, POST onboard-virtual-devices
  - inventory-service: POST notify-new-device
  - user-service: GET master-user-email
  - managed-software-service: POST insert-managed-software

---

## Inter-Service Call Map

```
                  ┌─────────────────┐
                  │  asset-service  │◄────────────────────────────────────────────────────────┐
                  │   (Port 8085)   │◄──────────────────────────────────────────────────────┐ │
                  └────────┬────────┘◄────────────────────────────────────────────────────┐ │ │
                           │ called by all 5 downstream services                          │ │ │
        ┌──────────────────┼─────────────────────────────────┐              │             │ │ │
        │                  │                                 │              │             │ │ │
┌───────▼──────┐  ┌────────▼──────────┐  ┌──────────────────▼──┐  ┌───────▼──────────┐  │ │ │
│asset-onboard │  │asset-onboarding   │  │asset-onboarding-ai  │  │device-lifecycle  │  │ │ │
│   -service   │  │   -service        │  │   -service          │  │   -service       │  │ │ │
│  (Port 8082) │  │  (Port 8083)      │  │  (Port 8084)        │  │  (dynamic port)  │  │ │ │
└──────────────┘  └──────────┬────────┘  └─────────────────────┘  └────────┬─────────┘  │ │ │
                             │ ▲                   │ ▲                      │            │ │ │
                             │ │ createMapping      │ │ getUnmappedAssets   │            │ │ │
                             │ └────────────────────┘ └────────────────────┘            │ │ │
                             │                                                           │ │ │
                             └───────────────────────────────────────────────────────────┘ │ │
                                                                                           │ │
┌─────────────────────────────────────────────────────────────────────────────────────────┘ │
│  device-specification-service (dynamic port)                                               │
│  → asset-service    → inventory-service    → user-service    → managed-software-service    │
└────────────────────────────────────────────────────────────────────────────────────────────┘
                                                                        │
                    ┌───────────────────────────────────────────────────▼────────────────┐
                    │   External / Other Microservices (not in this repo)                 │
                    │   inventory-service  |  user-service  |  managed-software-service   │
                    └─────────────────────────────────────────────────────────────────────┘
```

---

## Startup Order

```
1. discovery-service          (8761)  — Eureka must be up first
2. api-gateway                (8080)  — needs Eureka to resolve lb:// routes
3. asset-service              (8085)  — core dependency for all other services
4. asset-onboard-service      (8082)  ─┐
5. asset-onboarding-service   (8083)  ─┤  can start in any order after asset-service
6. asset-onboarding-ai-service(8084)  ─┤
7. device-lifecycle-service   (dyn)   ─┤
8. device-specification-service(dyn)  ─┘
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
All routes are accessible via `http://localhost:8080`. The gateway proxies to the correct downstream service transparently.

### asset-service (Port 8085)
| Method | Path | Purpose |
|--------|------|---------|
| GET    | /api/devices/{id}?vdmsId= | Get device by ID |
| GET    | /api/devices?vdmsId=&dockerId=&search=&page=&size= | Filtered paginated list |
| POST   | /api/devices?username= | Create device |
| PUT    | /api/devices/{id}?username= | Full update |
| PATCH  | /api/devices/{id}/onboard-status | Patch onboard_status |
| PATCH  | /api/devices/{id}/asset-match-status | Patch asset_match_status |
| DELETE | /api/devices/{id} | Delete device |
| GET    | /api/devices/onboard-count?dockerId= | Counts by onboard status |

### asset-onboard-service (Port 8082)
| Method | Path | Purpose |
|--------|------|---------|
| POST   | /api/onboard/assets/upsert?vdmsId= | Upsert onboard records |
| PUT    | /api/onboard/devices/{id}/data?vdmsId=&onboardStatus= | Update image/geo/tag/field data |
| GET    | /api/onboard/devices/{id}/status?vdmsId= | Get onboard status for device |
| GET    | /api/onboard/assignees?vdmsId= | List assignee emails |

### asset-onboarding-service (Port 8083)
| Method | Path | Purpose |
|--------|------|---------|
| GET    | /api/assets?vdmsId=&importType=&search=&page=&size= | List assets (paginated) |
| GET    | /api/assets/unmapped?vdmsId= | Assets not yet mapped to a device |
| POST   | /api/assets | Upsert asset |
| DELETE | /api/assets/{id} | Delete asset + its mappings |
| POST   | /api/assets/mappings?vdmsId= | Create asset-device mapping |
| DELETE | /api/assets/mappings/{id}?vdmsId= | Delete mapping |
| GET    | /api/assets/mappings?deviceId= | Mappings for a given device |
| GET    | /api/assets/fields | Asset field definitions |

### asset-onboarding-ai-service (Port 8084)
| Method | Path | Purpose |
|--------|------|---------|
| POST   | /api/ai/suggest | Ranked match suggestions for a device |
| POST   | /api/ai/batch-match?vdmsId= | Auto-match all AI-enabled devices in a tenant |

### device-lifecycle-service (dynamic port)
| Method | Path | Purpose |
|--------|------|---------|
| POST   | /api/device-lifecycle/user/{username}/vdms/{vdmsId}/adddevicehistory?retireStatus= | Add lifecycle event (assign / unassign / retire) |
| GET    | /api/device-lifecycle/user/{username}/vdms/{vdmsId}/device/{deviceId}/getdevicehistory?pageNo=&pageSize= | Paginated lifecycle history |
| GET    | /api/device-lifecycle/device/{deviceId}/lifecycle/latest-status | Latest operational status |
| DELETE | /api/device-lifecycle/device/{deviceId}/lifecycle/history | Delete all history for a device |

### device-specification-service (dynamic port)
| Method | Path | Purpose |
|--------|------|---------|
| POST   | /api/device-specification/devicespecification | Ingest full hardware/software spec from agent |
| POST   | /api/device-specification/deltadevicespecs | Apply incremental delta spec update |
| GET    | /api/device-specification/devicespecification/{deviceId} | Get full spec for a device |
| GET    | /api/device-specification/systemupdates/{deviceId} | Get pending system updates |
| PUT    | /api/device-specification/{sn}/device/{deviceId} | Link spec (by serial number) to a device ID |
| DELETE | /api/device-specification/device/{deviceId} | Delete spec + network spec + installed apps |
| GET    | /api/device-specification/emails | Distinct user emails in specs |
| GET    | /api/device-specification/os-types | Distinct OS types in specs |
