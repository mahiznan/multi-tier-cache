# Multi-Tier Cache Simulation Guide

## PHASE 0 — Prerequisites

### You Already Have

- Docker
- Docker Compose

### You Will Run

- **Nginx** → edge / cache
- **Backend service** → simulates DB + logic
- **Redis** → shared cache
- **Local in-memory cache** → per “region”
- **Kafka / Redis Streams (later)** → invalidation events

---

## PHASE 1 — Baseline (No Cache)

### Goal

Understand baseline latency and load.

### Setup

Client → Nginx → Backend → “DB”

### Backend

- Simple REST service
- Simulate DB with:
  - `sleep(100ms)`
  - Static JSON response

### Tests

- Send **100 concurrent requests**
- Observe:
  - Latency
  - CPU usage
  - Requests/sec

> ✅ This is your **control group**.

---

## PHASE 2 — Nginx HTTP Cache (Edge Cache)

### Goal

Learn read caching at the edge.

### Example Nginx Config

proxy_cache_path /data/nginx levels=1:2 keys_zone=my_cache:10m inactive=60s;

location /data {
proxy_cache my_cache;
proxy_cache_valid 200 10s;
proxy_pass http://backend;
}

### Tests

- Hit `/data` repeatedly.

### Observe

- `X-Cache-Status: HIT | MISS`
- Restart backend → cache still serves.

### Learning

- TTL-based.
- No invalidation.
- Fast but stale-prone.

---

## PHASE 3 — Redis as Central Cache (Cache-Aside)

### Goal

Implement the industry-standard **cache-aside** pattern.

### Flow

Request → Nginx → Backend

Backend logic:

1. Redis `GET`
2. On miss → fetch from DB → Redis `SET`

### Redis Key Pattern

user:{id}

TTL: **30 seconds**

### Tests

- First request → slow (cache miss).
- Subsequent requests → fast (cache hit).
- Kill Redis → fallback to DB.
- Restart Redis → cache refilled.

### Learning

- Classic cache-aside behavior.
- Failure-tolerant.

---

## PHASE 4 — Invalidate-on-Write (Meta / Netflix Style)

### Goal

Enable **targeted invalidation**.

### Flow

Write → DB → Publish invalidation
Read → Cache miss → Refill

### Implementation

On write:
redis DEL user:{id}

Or publish via Pub/Sub:

### Tests

- Warm cache.
- Update data.
- Verify:
  - Cache cleared.
  - Next read fetches fresh data.

### Learning

- Cheap writes.
- Small staleness window.

---

## PHASE 5 — Versioned Cache (Google-Style)

### Goal

Avoid full invalidation using version control.

### Cache Entry Example

{
"version": 42,
"payload": { ... }
}

### Flow

- DB holds authoritative version.
- Writes increment version.
- Publish `(key, version)`.
- Cache refreshes only if `version < latest`.

### Tests

- Simulate multiple readers.
- Partial updates.
- Observe fewer cache misses than invalidation.

### Learning

- Higher complexity.
- Lower cache churn.

---

## PHASE 6 — Multi-Region Simulation (Local)

### Goal

Understand regional cache behaviors.

### Setup

nginx-region-1 → backend-1 → redis
nginx-region-2 → backend-2 → redis

Each backend has:

- Local in-memory cache.
- Redis fallback.

### Tests

- Read from Region 1.
- Update data.
- Observe Region 2 staleness.
- Trigger invalidation broadcast.

### Learning

- Eventual consistency.
- Region isolation.

---

## PHASE 7 — Event-Driven Invalidation (Amazon / Google)

### Goal

Decouple writes from cache state.

### Add

- Redis Streams or Kafka.

### Flow

Write → DB → Publish event
Consumers → Invalidate or refresh cache

### Tests

- Kill one consumer.
- Restart it.
- Replay events.
- Validate cache convergence.

### Learning

- Durable invalidation.
- At-least-once delivery.

---

## PHASE 8 — Soft TTL + Background Refresh (Apple / Netflix)

### Goal

Prevent cache stampede at expiry.

### Technique

- **Soft TTL:** serve stale responses.
- **Hard TTL:** force refresh.

### Implementation

- Serve cached data immediately.
- Trigger async refresh when near expiry.

### Tests

- Simulate burst traffic at TTL expiry.
- Confirm:
  - No DB stampede.
  - Low tail latency.

---

## PHASE 9 — Failure Scenarios (Real-World)

### Test Cases

| Scenario       | Expected Behavior      |
| -------------- | ---------------------- |
| Redis down     | Local cache serves     |
| Pub/Sub lag    | Temporary staleness    |
| Region failure | Other regions continue |
| DB slow        | Cache absorbs load     |

---

## PHASE 10 — Metrics & Observability

### Add

- Cache hit ratio
- Invalidation lag
- P99 latency

### Use

- Nginx logs
- Redis `INFO`
- Simple Prometheus (optional)

---

## Recommended Order of Implementation

1. Nginx cache
2. Redis cache-aside
3. Invalidate-on-write
4. Versioned cache
5. Multi-region
6. Event-driven invalidation
7. Soft TTL

---

## Interview-Level Summary Line

> “I built a local multi-tier cache simulation using Nginx, Redis, and event-driven invalidation to study TTL, invalidate-on-write, versioned caching, and multi-region consistency trade-offs.”

---

## Next Step

If you’d like, I can:

- Provide `docker-compose.yml`
- Provide Nginx configs
- Provide sample backend code
- Give `curl` test scripts

Tell me what you want next and I’ll go concrete.
