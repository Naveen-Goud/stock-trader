# Stock Trading Simulator — Git & Run Commands

## Fixes applied in this revision

This revision resolves every issue hit during local Docker Compose testing:

1. **`Dockerfile.backend`** — Maven wasn't installed in the `eclipse-temurin:21-jdk-alpine`
   builder image (`apk add --no-cache maven` added), and the parent POM wasn't installed
   before `common-lib` tried to resolve it (`mvn install -N` step added before building
   `common-lib`, which itself is now installed before the target service is packaged).
2. **`Dockerfile.frontend`** — `nginx.conf` now lives inside `frontend/stock-trading-ui/`
   (the actual Docker build context for that image) instead of being referenced via a
   path outside the build context, which Docker cannot reach.
3. **`UserMapper`** — converted from a MapStruct `@Mapper` interface to a plain Spring
   `@Component` class. MapStruct's annotation processor is a known fragility point in
   multi-module Docker builds; a hand-written mapper removes that failure mode entirely.
4. **`UserProfileController`** — `X-User-Id` header is now `required = false` with an
   explicit 401 response when absent, instead of letting Spring throw an unhandled
   `MissingRequestHeaderException` that surfaced as an opaque 500.
5. **`GlobalExceptionHandler` (user-service)** — added a handler for
   `MissingRequestHeaderException` so any future missing-header case returns a clean
   401 instead of a 500.
6. **`JwtGatewayFilter`** — added `/api/market` and `/ws` to the public path allowlist
   (market data browsing and the WebSocket handshake should not require a Bearer token
   at the gateway layer — WebSocket auth happens separately at the STOMP CONNECT frame),
   and added an explicit bypass for `Upgrade: websocket` requests.
7. **`UserRepository`** — username lookups (`findByUsername`, `existsByUsername`) are
   now case-insensitive, so `Naveen` and `naveen` are treated as the same account both
   at registration (no duplicate-with-different-case accounts) and at login.
8. **`docker-compose.yml`** — `user-service` now exposes port `8081:8080` on the host,
   so it can be hit directly for debugging, bypassing the gateway entirely.

---

## 1. Initialize as a Git repository and push a PR

```bash
# Unzip the project (if you downloaded the zip)
unzip stock-trading-simulator.zip
cd stock-trading-simulator

# Initialize git
git init
git add .
git commit -m "feat: initial production scaffolding of Real-Time Stock Trading Simulator

- 5 Spring Boot 3 / Java 21 microservices (User, Trading, Portfolio, Market Data, Notification)
- Event-driven with Kafka (outbox pattern, DLT, consumer-side dedup)
- JWT auth with opaque refresh token rotation and reuse detection
- GBM price simulation (Market Data Service)
- WebSocket notifications via STOMP/SockJS
- React 18 + TypeScript + MUI + Redux Toolkit + React Query frontend
- Docker Compose for local dev; Kubernetes manifests (Kustomize) for production
- GitHub Actions CI (test → build → push) and CD (deploy → rollback)
- Prometheus + Grafana observability"

# Create remote (replace with your actual GitHub repo URL)
git remote add origin https://github.com/YOUR_USERNAME/stock-trading-simulator.git
git push -u origin main

# Create a feature branch and open a PR
git checkout -b feature/initial-implementation
git push origin feature/initial-implementation
# Then open: https://github.com/YOUR_USERNAME/stock-trading-simulator/compare/feature/initial-implementation
```

## 2. Run locally with Docker Compose (recommended — one command)

Prerequisites: Docker Desktop running (Mac/Windows) or Docker Engine (Linux).

```bash
cd stock-trading-simulator/infra

# (Optional) set a real JWT secret
export JWT_SECRET="my-super-secret-jwt-key-at-least-32-chars"

# Start everything: Postgres, Redis, Kafka, all 5 services, frontend, Prometheus, Grafana
docker compose up --build

# OR start infra only first, then services individually for faster iteration:
docker compose up postgres redis kafka zookeeper service-discovery

# Then in separate terminals:
docker compose up user-service
docker compose up trading-service
docker compose up portfolio-service
docker compose up market-data-service
docker compose up notification-service
```

Services come up in this order (health checks enforce it):
1. Postgres → Redis → Kafka (infrastructure)
2. service-discovery (Eureka)
3. All 5 business services + api-gateway (in parallel)
4. frontend

Once healthy, access:
- **Frontend**: http://localhost:3000
- **API**: http://localhost:8080
- **Eureka dashboard**: http://localhost:8761
- **Kafka UI**: http://localhost:8090
- **Grafana**: http://localhost:3001 (admin / admin)
- **Prometheus**: http://localhost:9090

## 3. Run locally without Docker (dev mode)

### Prerequisites
- Java 21 (JDK, not just JRE)
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16 running locally (port 5432)
- Redis running locally (port 6379)
- Kafka running locally (port 9092)

### Step A — Create the five databases in Postgres
```bash
psql -U postgres -c "CREATE DATABASE user_db;"
psql -U postgres -c "CREATE DATABASE trading_db;"
psql -U postgres -c "CREATE DATABASE portfolio_db;"
psql -U postgres -c "CREATE DATABASE marketdata_db;"
psql -U postgres -c "CREATE DATABASE notification_db;"
```

### Step B — Build common-lib (all services depend on it)
```bash
cd stock-trading-simulator/backend
mvn -pl common-lib -am clean install -DskipTests
```

### Step C — Start services (each in its own terminal)

```bash
# Terminal 1 — Service Discovery (start this first)
cd backend/service-discovery
mvn spring-boot:run

# Terminal 2 — API Gateway (after Eureka is up at :8761)
cd backend/api-gateway
mvn spring-boot:run

# Terminal 3 — User Service
JWT_SECRET=dev-secret-at-least-32-characters-long \
mvn -pl user-service spring-boot:run -pl ../user-service

# Terminal 4 — Trading Service
JWT_SECRET=dev-secret-at-least-32-characters-long \
KAFKA_BROKERS=localhost:9092 \
mvn spring-boot:run

# Terminal 5 — Portfolio Service
KAFKA_BROKERS=localhost:9092 \
mvn spring-boot:run

# Terminal 6 — Market Data Service
KAFKA_BROKERS=localhost:9092 \
mvn spring-boot:run

# Terminal 7 — Notification Service
KAFKA_BROKERS=localhost:9092 \
mvn spring-boot:run

# Or from the backend/ root, run the full multi-module build:
cd stock-trading-simulator/backend
mvn clean install -DskipTests   # builds all modules
```

### Step D — Start frontend
```bash
cd stock-trading-simulator/frontend/stock-trading-ui
npm install
cp .env.example .env            # defaults to http://localhost:8080 — no changes needed for local dev
npm run dev
```
Frontend available at http://localhost:3000.

## 4. Running tests

### Backend (per service, from backend/ root)
```bash
cd stock-trading-simulator/backend

# Run all tests (requires Postgres + Kafka for TestContainers integration tests)
mvn test

# Unit tests only (no external dependencies needed)
mvn test -Dtest="*Test" -Dsurefire.failIfNoSpecifiedTests=false

# Single service
mvn -pl user-service test

# With coverage report (generates target/site/jacoco/index.html)
mvn verify
```

### Frontend
```bash
cd stock-trading-simulator/frontend/stock-trading-ui
npm test                  # run all tests
npm test -- --ui          # interactive Vitest UI in browser
npm run test -- --coverage  # with coverage report
```

## 5. Deploy to Kubernetes

```bash
# Prerequisites: kubectl configured for your cluster, images built and pushed

# Build and push images (replace registry URL)
cd stock-trading-simulator/backend
docker build -f ../infra/docker/Dockerfile.backend --build-arg SERVICE_NAME=user-service \
  -t ghcr.io/YOUR_ORG/user-service:latest .
# ... repeat for each service

# Edit the secret (never commit real values)
vim infra/k8s/base/secret.yaml

# Deploy via Kustomize
kubectl apply -k infra/k8s/overlays/prod

# Monitor rollout
kubectl rollout status deployment/trading-service -n stock-sim

# Roll back if needed
kubectl rollout undo deployment/trading-service -n stock-sim
```

## 6. Environment variable reference

| Variable | Default | Used by |
|---|---|---|
| `JWT_SECRET` | `change-this-to-a-256-bit-secret-in-production-env` | All services + gateway |
| `DB_USER` | `postgres` | All DB-connected services |
| `DB_PASSWORD` | `postgres` | All DB-connected services |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/<db_name>` | Each service |
| `REDIS_HOST` | `localhost` | All services using Redis |
| `KAFKA_BROKERS` | `localhost:9092` | Trading, Portfolio, Market Data, Notification |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://localhost:8761/eureka/` | All services |
| `VITE_API_BASE_URL` | `http://localhost:8080` | Frontend |
| `VITE_WS_BASE_URL` | `http://localhost:8080` | Frontend |


## Troubleshooting

### "unhealthy" status in `docker compose ps`
Spring Boot services can take 30-60 seconds to fully start (Flyway migrations,
Eureka registration, Kafka consumer group join). Check actual readiness with:
```bash
docker compose logs <service-name> --tail=50
```
Look for `Started <ServiceName>Application` — if that line is present, the
service is actually up even if the Docker healthcheck hasn't caught up yet.

### Testing the backend without the frontend
```bash
# Direct to user-service (bypasses gateway entirely)
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@test.com","password":"Test@1234"}'

curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@1234"}'

# Through the gateway (what the frontend actually calls)
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@1234"}'
```
If port 8081 works but 8080 doesn't, the bug is in `JwtGatewayFilter` or the
gateway's routing config. If neither works, the bug is in `user-service` itself.

### Windows PowerShell curl syntax
PowerShell aliases `curl` to `Invoke-WebRequest`, which uses different flags.
Either use `curl.exe` explicitly (the real curl binary) with the Linux-style
syntax above, or use native PowerShell syntax:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/users/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"testuser","password":"Test@1234"}'
```

### Forcing a clean rebuild after editing any Dockerfile
Docker caches build layers aggressively. After editing a Dockerfile, always:
```bash
docker compose build --no-cache <service-name>
docker compose up -d <service-name>
```
Plain `docker compose up --build` is not always sufficient to bust the cache.

### Checking what databases actually exist
```bash
docker exec infra-postgres-1 psql -U postgres -c "\l"
```
Should list: `user_db`, `trading_db`, `portfolio_db`, `marketdata_db`, `notification_db`

### Inspecting a user record directly
```bash
docker exec infra-postgres-1 psql -U postgres -d user_db -c "SELECT id, username, email, role, wallet_balance FROM users;"
```
