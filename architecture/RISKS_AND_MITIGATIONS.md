# Risks & Mitigations

## 1. Distributed Transaction Integrity (HIGH RISK)

**Scenario:** Device deletion must cascade to:
- `device_onboard_status` (asset-onboard-service)
- `asset_device_mapping` (asset-onboarding-service)

**Risk:** With shared DB but separate services, cascades no longer happen automatically.
A DELETE on `device` will violate FK constraints if child records exist (unless FK is removed).

**Mitigation Options:**
| Option | Trade-off |
|--------|-----------|
| **Remove FK constraints; use soft delete** | Fast; no cascade needed; requires cleanup job |
| **Saga choreography** (each service reacts to `DeviceDeleted` event via RabbitMQ) | Eventually consistent; complex rollback |
| **Two-phase delete**: Coordinator calls dependants first, then deletes Device | Synchronous; simpler; partial failure possible |
| **Outbox pattern** | Guarantees at-least-once event delivery |

**Recommended:** Soft-delete (`is_removed=true`) on all tables + async cleanup job.

---

## 2. API Gateway – Single Point of Failure (HIGH RISK)

**Scenario:** All client traffic flows through `api-gateway` (port 8080). If it crashes or is
unreachable, all 4 downstream services become inaccessible regardless of their health.

**Risk:** One misconfigured route or OOM in the gateway takes down the entire system.

**Mitigation:**
- Run multiple gateway instances behind a load balancer in production
- Add `/actuator/health` monitoring on the gateway
- Keep gateway stateless and configuration-only (no business logic)
- Set per-route timeouts to prevent a slow downstream from exhausting gateway threads

---

## 3. Eureka Service Discovery – Discovery Lag (MEDIUM RISK)

**Scenario:** When a service restarts or crashes, the api-gateway may still route to the
stale instance for up to 90 seconds (Eureka's default lease expiry).

**Risk:** Requests routed to a dead instance return connection errors.

**Mitigation:**
```yaml
# Tune in each service's application.yml
eureka:
  instance:
    lease-renewal-interval-in-seconds: 10   # heartbeat frequency
    lease-expiration-duration-in-seconds: 30 # time before Eureka evicts dead instance
  client:
    registry-fetch-interval-seconds: 5      # how often gateway fetches the registry
```
- Combine with Spring Cloud LoadBalancer retry: retry once on a different instance on failure

---

## 4. Data Consistency – Shared DB (MEDIUM RISK)

**Risk:** Two services can write to related tables simultaneously with no cross-service locks.

**Mitigation:**
- Each service owns its tables exclusively — other services only call the owning service's API
- Never bypass a service to write directly to another service's table
- Use optimistic locking (`@Version`) on high-contention records

---

## 5. Tight Coupling via Synchronous WebClient (MEDIUM RISK)

**Risk:** If asset-service is down, asset-onboard-service and asset-onboarding-service fail.

**Mitigation:**
- Circuit breaker: Add **Resilience4j** `@CircuitBreaker` on WebClient calls
- Fallback: Return cached/stale data or HTTP 503 with `Retry-After` header
- Timeouts: Set `connectTimeout` and `readTimeout` on WebClient

```yaml
# Example Resilience4j config
resilience4j:
  circuitbreaker:
    instances:
      asset-service:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

---

## 6. Idempotency (LOW RISK)

**Risk:** Retry storms on network failure can cause duplicate records.

**Mitigation:**
- All write endpoints accept an `Idempotency-Key` header
- Use `INSERT ... ON DUPLICATE KEY UPDATE` (native upsert) pattern already in repo
- Mapping creation checks for existing record before insert

---

## 7. Rollback / Saga

If asset-service DELETE succeeds but asset-onboard-service cleanup fails:

```
Compensating Transaction:
  1. Re-create device_onboard_status with is_removed=false
  2. Alert via RabbitMQ dead-letter queue
  3. Background reconciliation job re-tries cleanup
```
