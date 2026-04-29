# Sequence Diagrams

## 1. Asset Onboarding Flow

```
Client        api-gateway      asset-onboard-svc        asset-service        asset-onboarding-svc
  │                │                  │                       │                       │
  │──POST /upsert─►│                  │                       │                       │
  │                │──route──────────►│                       │                       │
  │                │                  │──GET /devices/{id}───►│                       │
  │                │                  │◄─200 DeviceDTO────────│                       │
  │                │                  │──save onboard_status──►DB                     │
  │                │                  │──PATCH /onboard-status►│                     │
  │                │                  │◄─204──────────────────│                      │
  │◄─200───────────│◄─────────────────│                       │                       │
  │                │                  │                       │                       │
  │──PUT /devices/{id}/data──────────►│                       │                       │
  │                │                  │──GET /devices/{id}───►│                       │
  │                │                  │◄─200 DeviceDTO────────│                       │
  │                │                  │──UPDATE status fields──►DB                    │
  │                │                  │──PATCH /onboard-status►│                     │
  │◄─200───────────│◄─────────────────│                       │                       │
```

---

## 2. Device Deletion Flow (Critical – Transaction Boundary Risk)

```
Client      api-gateway     asset-service        asset-onboard-svc    asset-onboarding-svc   device-lifecycle-svc   device-spec-svc
  │               │               │                     │                     │                     │                    │
  │─DELETE /dev/─►│               │                     │                     │                     │                    │
  │               │──route───────►│                     │                     │                     │                    │
  │               │               │                     │                     │                     │                    │
  │               │         [STEP 1] Check dependencies (sync WebClient calls)
  │               │               │──GET /onboard/devices/{id}/status──────►│                     │                    │
  │               │               │◄─200 / 404──────────────────────────────│                     │                    │
  │               │               │──GET /assets/mappings?deviceId={id}─────────────────────────►│                    │
  │               │               │◄─200 List<Mapping>─────────────────────────────────────────│                    │
  │               │               │──GET /device-lifecycle/device/{id}/lifecycle/latest-status───────────────────►│   │
  │               │               │◄─200 / 404──────────────────────────────────────────────────────────────────│   │
  │               │               │──GET /device-specification/devicespecification/{id}────────────────────────────────►│
  │               │               │◄─200 / 404────────────────────────────────────────────────────────────────────────│
  │               │               │                     │                     │                     │                    │
  │               │         [STEP 2] Cascade deletes (best-effort; no distributed TX)
  │               │               │──DELETE /onboard/devices/{id}──────────►│                     │                    │
  │               │               │──DELETE /assets/mappings/all────────────────────────────────►│                    │
  │               │               │──DELETE /device-lifecycle/device/{id}/lifecycle/history────────────────────────►│  │
  │               │               │──DELETE /device-specification/device/{id}────────────────────────────────────────────►│
  │               │               │                     │                     │                     │                    │
  │               │         [STEP 3] Delete device row
  │               │               │──DELETE device WHERE id=? ─►DB            │                     │                    │
  │◄─204──────────│◄──────────────│                     │                     │                     │                    │
  │               │               │                     │                     │                     │                    │
  ⚠ If step 3 fails after step 2: orphaned records exist in other services.
    Mitigated by: compensating cleanup job + idempotent delete endpoints.
```

---

## 3. AI Batch Match Flow

```
Client      api-gateway      ai-service          asset-service     asset-onboarding-svc
  │               │               │                    │                   │
  │─POST /batch──►│               │                    │                   │
  │               │──route───────►│                    │                   │
  │               │               │──GET /devices?vdmsId=──────────────►│             │
  │               │               │◄─PageResponse<Device>───────────────│             │
  │               │               │──GET /assets/unmapped?vdmsId=───────────────────────────────►│
  │               │               │◄─List<Asset>────────────────────────────────────────────────│
  │               │               │                    │                   │
  │               │         [For each device × asset pair: compute score]
  │               │               │                    │                   │
  │               │         [If score >= threshold:]
  │               │               │──POST /assets/mappings?vdmsId=──────────────────────────────►│
  │               │               │◄─201 AssetDeviceMapping─────────────────────────────────────│
  │◄─200──────────│◄──────────────│                    │                   │
```

---

## 4. Service Registration Flow (Startup)

```
discovery-service   api-gateway   asset-service   asset-onboard-svc   asset-onboarding-svc   ai-service   lifecycle-svc   spec-svc
      │                  │               │                 │                    │                  │              │             │
 [starts first]          │               │                 │                    │                  │              │             │
      │◄──register───────│               │                 │                    │                  │              │             │
      │◄──register────────────────────── │                 │                    │                  │              │             │
      │◄──register─────────────────────────────────────────│                    │                  │              │             │
      │◄──register──────────────────────────────────────────────────────────────│                  │              │             │
      │◄──register───────────────────────────────────────────────────────────────────────────────── │              │             │
      │◄──register──────────────────────────────────────────────────────────────────────────────────────────────── │             │
      │◄──register─────────────────────────────────────────────────────────────────────────────────────────────────────────────── │
      │                  │               │                 │                    │                  │              │             │
      │  [all 8 instances visible in Eureka dashboard @ :8761]
      │                  │               │                 │                    │                  │              │             │
      │  [api-gateway resolves lb://asset-service              → :8085]
      │  [api-gateway resolves lb://asset-onboard-service      → :8082]
      │  [api-gateway resolves lb://asset-onboarding-service   → :8083]
      │  [api-gateway resolves lb://asset-onboarding-ai-service→ :8084]
      │  [api-gateway resolves lb://device-lifecycle-service   → dynamic]
      │  [api-gateway resolves lb://device-specification-service→ dynamic]
```

---

## 5. Device Lifecycle – Assign / Retire Flow

```
Client      api-gateway     device-lifecycle-svc    asset-service     inventory-service
  │               │                  │                    │                   │
  │─POST addHistory?retireStatus=───►│                    │                   │
  │               │──route──────────►│                    │                   │
  │               │                  │──repo.getLatestAssignmentCount()──►DB  │
  │               │                  │                    │                   │
  │               │           [if operationalStatus changed]
  │               │                  │──PATCH /api/devices/{id}/operational-status────────────►│
  │               │                  │◄─204──────────────────────────────────────────────────│
  │               │                  │                    │                   │
  │               │           [if retireStatus=true or false]
  │               │                  │──PATCH /api/devices/{id}/assigned-user-email (null)────►│
  │               │                  │                    │                   │
  │               │           [if retireStatus=true]
  │               │                  │──GET /api/devices/{id}────────────────────────────────►│
  │               │                  │◄─DeviceDTO (inventory_tracking_id)────────────────────│
  │               │                  │──POST /api/devices/{id}/archive-devices───────────────►│
  │               │                  │──POST retire-inventory-device─────────────────────────────────────────►│
  │               │                  │◄─200──────────────────────────────────────────────────────────────────│
  │               │                  │                    │                   │
  │               │                  │──INSERT device_lifecycle_history──►DB  │
  │◄─200──────────│◄─────────────────│                    │                   │
```

---

## 6. Device Specification – Full Spec Ingest Flow (Agent → Service)

```
Agent       api-gateway   device-spec-svc    asset-service   inventory-svc   user-svc   managed-sw-svc
  │               │               │                │               │             │             │
  │─POST /devicespecification────►│                │               │             │             │
  │               │──route───────►│                │               │             │             │
  │               │               │──GET /api/devices by serial-number───────►│             │             │
  │               │               │◄─DeviceDTO (or null)──────────────────────│             │             │
  │               │               │                │               │             │             │
  │               │        [if deviceId == null (unknown device)]
  │               │               │──GET /api/device-specification/emails (master-user)──────────────────►│
  │               │               │◄─email────────────────────────────────────────────────────────────────│
  │               │               │──POST notify-new-device───────────────────────────────►│             │
  │               │               │  (fire-and-forget)                                     │             │
  │               │               │                │               │             │             │
  │               │        [if deviceId != null AND fields changed]
  │               │               │──PATCH /api/devices/{id} device-info (ip, mac, model …)──────────────►│
  │               │               │──PATCH /api/devices/{id}/set-device-online─────────────────────────►│ │
  │               │               │                │               │             │             │
  │               │               │──UPSERT device_specification──►DB           │             │
  │               │               │──UPSERT device_network_specification─►DB    │             │
  │               │               │                │               │             │             │
  │               │        [for each new installed app]
  │               │               │──POST /insertManagedSoftware──────────────────────────────────────────────────────►│
  │               │               │◄─managedSoftwareId────────────────────────────────────────────────────────────────│
  │               │               │──INSERT device_installed_apps─►DB           │             │
  │               │               │                │               │             │             │
  │               │        [sync child/virtual devices]
  │               │               │──GET /api/devices findByDisplayNameAndParentId───────────►│             │
  │               │               │──POST /api/devices/addVirtualDevice (if not found)───────►│             │
  │               │               │──POST /api/onboard/onboardVirtualDevices───────────────►│              │
  │◄─200 deviceId─│◄──────────────│                │               │             │             │
```

---

## 7. Device Deletion with Lifecycle & Spec Cleanup – Compensating Flow

```
asset-service                 device-lifecycle-svc    device-spec-svc
      │                               │                     │
      │──DELETE lifecycle history────►│                     │
      │◄─200 / error──────────────────│                     │
      │──DELETE spec + network + apps─────────────────────►│
      │◄─200 / error──────────────────────────────────────│
      │                               │                     │
[if any step errors:]
      │──publish DeviceDeletion to dead-letter queue        │
      │  background job re-tries until all cleanup confirmed│
```
