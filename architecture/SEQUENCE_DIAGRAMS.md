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
  │──PUT /devices/{id}/data──────────►│               │                       │
  │                │                  │──GET /devices/{id}───►│                       │
  │                │                  │──UPDATE status fields──►DB                    │
  │                │                  │──PATCH /onboard-status►│                     │
  │◄─200───────────│◄─────────────────│                       │                       │
```

## 2. Device Deletion Flow (Critical – Transaction Boundary Risk)

```
Client      api-gateway     asset-service        asset-onboard-svc    asset-onboarding-svc
  │               │               │                     │                     │
  │─DELETE /dev/─►│               │                     │                     │
  │               │──route───────►│                     │                     │
  │               │               │                     │                     │
  │               │         [STEP 1] Check dependencies (sync WebClient calls)
  │               │               │──GET /onboard/devices/{id}/status──►│    │
  │               │               │◄─200 / 404──────────────────────────│    │
  │               │               │──GET /assets/mappings?deviceId={id}──────►│
  │               │               │◄─200 List<Mapping>───────────────────────│
  │               │               │                     │                     │
  │               │         [STEP 2] Cascade deletes (best-effort; no distributed TX)
  │               │               │──DELETE /onboard/devices/{id}──────►│    │
  │               │               │──DELETE /assets/mappings/all──────────────►│
  │               │               │                     │                     │
  │               │         [STEP 3] Delete device row
  │               │               │──DELETE device WHERE id=? ─►DB            │
  │◄─204──────────│◄──────────────│                     │                     │
  │               │               │                     │                     │
  ⚠ If step 3 fails after step 2: orphaned records exist in other services.
    Mitigated by: compensating cleanup job + idempotent delete endpoints.
```

## 3. AI Batch Match Flow

```
Client      api-gateway      ai-service          asset-service     asset-onboarding-svc
  │               │               │                    │                   │
  │─POST /batch──►│               │                    │                   │
  │               │──route───────►│                    │                   │
  │               │               │──GET /devices?vdmsId=──►│             │
  │               │               │◄─PageResponse<Device>───│             │
  │               │               │──GET /assets/unmapped?vdmsId=──────────►│
  │               │               │◄─List<Asset>─────────────────────────│
  │               │               │                    │                   │
  │               │         [For each device × asset pair: compute score]
  │               │               │                    │                   │
  │               │         [If score >= threshold:]
  │               │               │──POST /assets/mappings?vdmsId=─────────►│
  │               │               │◄─201 AssetDeviceMapping────────────────│
  │◄─200──────────│◄──────────────│                    │                   │
```

## 4. Service Registration Flow (Startup)

```
discovery-service    api-gateway    asset-service    asset-onboard-svc    ...others
       │                  │               │                 │
  [starts first]          │               │                 │
       │◄──register───────│               │                 │
       │◄──register────────────────────── │                 │
       │◄──register─────────────────────────────────────────│
       │                  │               │                 │
       │  [all instances visible in Eureka dashboard @ :8761]
       │                  │               │                 │
       │  [api-gateway resolves lb://asset-service → 8085]
       │  [api-gateway resolves lb://asset-onboard-service → 8082]
       │  [...]
```

## 5. Device Lifecycle – Assign / Retire Flow

```
Client      api-gateway     device-lifecycle-svc    asset-service     inventory-service
  │               │                  │                    │                   │
  │─POST addHistory?retireStatus=───►│                    │                   │
  │               │──route──────────►│                    │                   │
  │               │                  │──repo.getLatestAssignmentCount()──►DB  │
  │               │                  │                    │                   │
  │               │           [if operationalStatus changed]
  │               │                  │──PATCH /operational-status────────►│   │
  │               │                  │◄─204──────────────────────────────│   │
  │               │                  │                    │                   │
  │               │           [if retireStatus=true or false]
  │               │                  │──PATCH /assigned-user-email (null)─►│  │
  │               │                  │                    │                   │
  │               │           [if retireStatus=true]
  │               │                  │──GET /devices/{id}────────────────►│   │
  │               │                  │◄─DeviceDTO (inventory_tracking_id)─│   │
  │               │                  │──POST /archive-devices─────────────►│  │
  │               │                  │──POST retire-inventory-device───────────►│
  │               │                  │◄─200──────────────────────────────────│
  │               │                  │                    │                   │
  │               │                  │──INSERT device_lifecycle_history──►DB  │
  │◄─200──────────│◄─────────────────│                    │                   │
```

## 6. Device Specification – Full Spec Ingest Flow (Agent → Service)

```
Agent       api-gateway   device-spec-svc    asset-service   inventory-svc   user-svc   managed-sw-svc
  │               │               │                │               │             │             │
  │─POST /devicespecification────►│                │               │             │             │
  │               │──route───────►│                │               │             │             │
  │               │               │──GET by serial-number─────────►│             │             │
  │               │               │◄─DeviceDTO (or null)──────────│             │             │
  │               │               │                │               │             │             │
  │               │        [if deviceId == null (unknown device)]
  │               │               │──GET master-user-email──────────────────────►│             │
  │               │               │◄─email────────────────────────────────────│             │
  │               │               │──POST notify-new-device────────────────────►│             │
  │               │               │  (fire-and-forget)                          │             │
  │               │               │                │               │             │             │
  │               │        [if deviceId != null AND fields changed]
  │               │               │──PATCH device-info (ip, mac, model …)──────►│             │
  │               │               │──PATCH set-device-online──────────────────►│             │
  │               │               │                │               │             │             │
  │               │               │──UPSERT device_specification──►DB           │             │
  │               │               │──UPSERT device_network_specification─►DB    │             │
  │               │               │                │               │             │             │
  │               │        [for each new installed app]
  │               │               │──POST insertManagedSoftware──────────────────────────────►│
  │               │               │◄─managedSoftwareId──────────────────────────────────────│
  │               │               │──INSERT device_installed_apps─►DB           │             │
  │               │               │                │               │             │             │
  │               │        [sync child/virtual devices]
  │               │               │──GET findByDisplayNameAndParentId─────────►│             │
  │               │               │──POST addVirtualDevice (if not found)──────►│             │
  │               │               │──POST onboardVirtualDevices───────────────►│             │
  │◄─200 deviceId─│◄──────────────│                │               │             │             │
```
