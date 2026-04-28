# Migration Plan – Strangler Fig Approach

## Phase 0: Preparation (Week 1-2)
- [ ] Add integration tests covering device, asset, and onboard workflows
- [ ] Instrument existing monolith with structured logging per domain
- [ ] Remove all FK constraints that cross domain boundaries (keep app-level validation)
- [ ] Add soft-delete `is_removed` column to `device_onboard_status`
- [ ] Stand up **discovery-service** (Eureka) and **api-gateway** (Spring Cloud Gateway)
- [ ] Verify all 6 services register with Eureka on startup

## Phase 1: Extract asset-service (Week 3-5)
- [ ] Copy Device entity + repository + service + controller to new Spring Boot project
- [ ] Flatten all ORM relationships to ID fields
- [ ] Deploy asset-service alongside monolith
- [ ] Route `/api/devices/**` via API gateway (`lb://asset-service`) to new service
- [ ] Monolith calls asset-service via WebClient for device reads/writes
- [ ] Run both in parallel; verify parity via shadow traffic

## Phase 2: Extract asset-onboarding-service (Week 6-8)
- [ ] Extract Asset, AssetDeviceMapping, AssetField domains
- [ ] asset-onboarding-service calls asset-service WebClient for device verification
- [ ] Route `/api/assets/**` via gateway to new service

## Phase 3: Extract asset-onboard-service (Week 9-10)
- [ ] Extract DeviceOnboardStatus + Assignee
- [ ] Wire WebClient calls to asset-service for status sync
- [ ] Route `/api/onboard/**` via gateway to new service

## Phase 4: Extract asset-onboarding-ai-service (Week 11-12)
- [ ] Stateless service; lowest risk
- [ ] Calls existing asset-service + asset-onboarding-service via `lb://` URIs
- [ ] Route `/api/ai/**` via gateway to new service

## Phase 5: Decommission Monolith Modules (Week 13-14)
- [ ] Remove device, asset, onboard modules from monolith
- [ ] Validate all 4 downstream routes through api-gateway
- [ ] Verify Eureka dashboard shows all 6 services healthy
- [ ] Load test all services independently and through the gateway

## Rollback Strategy
Each phase is independently revertable:
- API Gateway routing is the toggle — flip a route back to the monolith URI in < 1 min
- Discovery service deregistration is instant via Eureka REST API
- Database schema is additive only during migration
