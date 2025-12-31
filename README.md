# 🚀 Multi Tier Cache

[![Docker Compose](https://img.shields.io/badge/Docker-Compose-brightgreen)](https://docs.docker.com/compose/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-orange)](https://spring.io/projects/spring-boot)
[![Nginx](https://img.shields.io/badge/Nginx-1.25-blue)](https://hub.docker.com/_/nginx)
[![Redis](https://img.shields.io/badge/Redis-7-green)](https://hub.docker.com/_/redis)

**Multi Tier Cache** demonstrates a **high-performance API** with **three layers of caching**:
1. **Nginx** (L1: 10s TTL, disk-backed)
2. **Redis** (L2: 30s TTL, in-memory)
3. **Application** (simulated slow DB)

Built for developers who want to **ship fast APIs** with **automatic cache invalidation** and **graceful degradation**. 🎯

---

## 🏗️ Architecture Overview

Client → http://localhost:80/data?id=123
↓
[Nginx Cache - 10s TTL] ← X-Cache-Status: HIT/MISS
↓
[Spring Boot Service:8081] ← /data (GET/POST)
↓
[Redis Cache - 30s TTL] ← data:123
↓
[Slow DB - 100ms]

text

**Key Features:**
- ✅ **Nginx** serves stale cache during backend failures (`proxy_cache_use_stale`)
- ✅ **Redis** TTL auto-eviction + manual invalidation on POST
- ✅ **Multi-stage Dockerfile** - no local `mvn package` needed
- ✅ **Observability** - `X-Cache-Status` header shows cache layer

---

## 📂 Project Structure

├── docker-compose.yml # Orchestrates nginx + service + redis 🐳
├── execution-plan.md # High-level system design goals 📋
├── nginx/
│ └── nginx.conf # Reverse proxy + L1 cache (10s TTL) ⚡
└── service/ # Spring Boot app (Maven + Redis L2 cache)
├── Dockerfile # Multi-stage: Maven build → slim runtime 📦
├── pom.xml # Maven dependencies
├── mvnw* # Maven wrapper (no local Java needed)
└── src/
├── main/java/.../DataController.java # GET/POST /data
├── main/java/.../DataService.java # Redis + slow DB logic
└── main/resources/application.properties # server.port=8081

text

---

## 🚦 Prerequisites

- 🐳 **Docker** + **Docker Compose** (v2+)
- No Java/Maven/Redis needed locally - everything containerized! 🎉

---

## ▶️ Quick Start (30 seconds)

```bash
# Clone & cd into project
git clone <repo> && cd multi-tier-cache

# Build + start everything
docker compose up --build
Expected output:

text
nginx_1    | [notice] serving from cache: HIT/MISS visible in logs
service_1  | Started ServiceApplication in 2.345s
redis_1    | Ready to accept connections
🌐 API Endpoints
All requests go through Nginx (localhost:80):

🔍 GET Data (Read with cache)
bash
curl "http://localhost/data?id=123"
Response:

json
{
  "id": "123",
  "value": "value-from-db-1735700000000",
  "source": "REDIS"  // or "DB" on first hit
}
Headers to watch:

text
X-Cache-Status: HIT  // Nginx cache
✏️ POST Update (Write + Cache Invalidation)
bash
curl -X POST "http://localhost/data" \
  -d "id=123" \
  -d "value=new-value"
Behavior:

Updates "DB" (simulated)

Deletes Redis key data:123

Next GET will miss Redis → hit DB → repopulate cache

🔍 Direct Service Access (Bypass Nginx)
bash
curl "http://localhost:8081/data?id=123"
🧠 Cache Behavior Demo
Watch the three-tier cache in action:

bash
# 1️⃣ First request: MISS everywhere (slow)
curl "http://localhost/data?id=abc" -w "\nX-Cache: %{X-Cache-Status}\n"

# 2️⃣ Second request: Nginx HIT (fastest)
curl "http://localhost/data?id=abc" -w "\nX-Cache: %{X-Cache-Status}\n"

# 3️⃣ Invalidate via POST
curl -X POST "http://localhost/data?id=abc&value=updated"

# 4️⃣ GET again: Nginx MISS → Redis MISS → DB (medium)
curl "http://localhost/data?id=abc" -w "\nX-Cache: %{X-Cache-Status}\n"
Expected progression:

text
source: "DB",    X-Cache: MISS    # Cold start
source: "REDIS", X-Cache: HIT     # Nginx serves stale
source: "DB",    X-Cache: MISS    # After invalidation
🛠 Development Workflow
🔄 Code Changes (Hot Reload)
bash
# Edit service/src/... or nginx.conf
# Rebuild just the service
docker compose up --build service

# Or full rebuild
docker compose up --build
🧹 Stop / Clean
bash
# Graceful stop (cache volume preserved)
docker compose down

# Nuclear option (removes nginx-cache volume)
docker compose down -v
📊 Logs
bash
docker compose logs nginx      # Cache HIT/MISS
docker compose logs service    # Redis operations
docker compose logs redis      # Cache storage
⚙️ Configuration Deep Dive
Nginx (nginx/nginx.conf)
text
location /data {
  proxy_cache my_cache;                    # Enable L1 cache
  proxy_cache_valid 200 10s;              # 10s TTL
  proxy_cache_use_stale error timeout;    # Graceful degradation
  add_header X-Cache-Status $upstream_cache_status;  # Observability
  proxy_pass http://service:8081;          # → Spring Boot
}
Cache key: Full request (scheme+host+URI+params)

Disk: /var/cache/nginx (persists via Docker volume)

Spring Boot (service/src/main/resources/application.properties)
text
server.port=8081
spring.data.redis.host=redis
spring.data.redis.port=6379
Redis Logic (DataService.java)
java
String key = "data:" + id;
String cached = redisTemplate.opsForValue().get(key);  // L2 lookup
if (cached != null) return new DataResponse(id, cached, "REDIS");

simulateDbCall();  // 100ms slow DB
redisTemplate.opsForValue().set(key, dbValue, Duration.ofSeconds(30));  // Cache 30s
🎯 Why This Project?
Perfect starting point for:

✅ Building cache-aware APIs from day zero

✅ Understanding cache invalidation strategies

✅ Demonstrating multi-layer caching (Nginx + Redis)

✅ Graceful degradation during outages

✅ Observability-first design (X-Cache-Status)

Production-ready patterns:

Multi-stage Docker builds ✅

Docker Compose orchestration ✅

Cache-aside pattern ✅

TTL + manual invalidation ✅

Stale-while-revalidate ✅

🚀 Next Steps
Run it: docker compose up --build 🎉

Play with cache: Use the curl examples above 🔍

Extend it:

Add health checks to docker-compose.yml

Implement Nginx rate limiting

Add Spring Actuator endpoints

Deploy to Kubernetes or ECS

📚 Further Reading
execution-plan.md - System design goals & HA/throughput targets

service/HELP.md - Spring Boot specifics

Nginx Caching Docs - Deep dive

⭐ Star if helpful! This setup will make any developer productive in <1 minute. 🚀
