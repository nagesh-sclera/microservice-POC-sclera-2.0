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
