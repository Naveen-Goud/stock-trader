# Real-Time Stock Trading Simulator

A production-grade, microservices-based stock trading simulator built to demonstrate
distributed systems and full-stack engineering practices: event-driven architecture
(Kafka with outbox pattern), JWT auth with refresh token rotation, Redis caching
strategies, WebSocket real-time updates, and a Kubernetes-ready deployment pipeline.

## Architecture

- **User Service** (8081) — registration, JWT auth, refresh token rotation
- **Trading Service** (8083) — buy/sell execution, outbox pattern for Kafka publishing
- **Portfolio Service** (8084) — holdings projection, portfolio valuation, watchlists
- **Market Data Service** (8085) — GBM-based price simulation, price history
- **Notification Service** (8086) — Kafka consumers, WebSocket push, price alerts
- **API Gateway** (8080) — JWT validation, routing, internal-path blocking
- **Service Discovery** (8761) — Eureka server

See `backend/*/src/main/resources/db/migration` for schema, and each service's
controller package for REST endpoints.

## Quick Start (Local, via Docker Compose)

```bash
cd infra
docker compose up --build
```

This starts Postgres, Redis, Kafka (+ Kafka UI), all 5 backend services,
API Gateway, Service Discovery, the React frontend, and Prometheus + Grafana.

Once healthy:
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- Eureka dashboard: http://localhost:8761
- Kafka UI: http://localhost:8090
- Grafana: http://localhost:3001 (admin/admin)
- Prometheus: http://localhost:9090

## Quick Start (Backend only, without Docker)

Requires: Java 21, Maven, a running Postgres instance, Redis, and Kafka.

```bash
cd backend
mvn clean install -DskipTests          # builds common-lib + all services
cd service-discovery && mvn spring-boot:run &
cd ../api-gateway && mvn spring-boot:run &
cd ../user-service && mvn spring-boot:run &
cd ../trading-service && mvn spring-boot:run &
cd ../portfolio-service && mvn spring-boot:run &
cd ../market-data-service && mvn spring-boot:run &
cd ../notification-service && mvn spring-boot:run &
```

## Quick Start (Frontend only)

```bash
cd frontend/stock-trading-ui
npm install
cp .env.example .env     # adjust VITE_API_BASE_URL / VITE_WS_BASE_URL if needed
npm run dev
```

## Running Tests

```bash
# Backend (per service)
cd backend/trading-service && mvn test

# Frontend
cd frontend/stock-trading-ui && npm run test
```

## Kubernetes Deployment

```bash
kubectl apply -k infra/k8s/overlays/prod
```

Requires images to be built and pushed first (see `.github/workflows/ci.yml`
for the exact build commands), and `infra/k8s/base/secret.yaml` populated with
real values (never commit real secrets — use Sealed Secrets or an external
secrets manager in actual production use).

## Key Design Decisions

- **Outbox pattern** in Trading Service guarantees at-least-once Kafka delivery
  without a dual-write race between the DB commit and the Kafka publish.
- **Opaque, rotating refresh tokens** (not JWTs) stored hashed server-side,
  enabling immediate revocation and reuse detection.
- **Geometric Brownian Motion** price simulation in Market Data Service for
  realistic, per-stock-tunable volatility.
- **Database-per-service** — no service queries another's tables directly;
  all cross-service consistency flows through Kafka events or narrow internal
  REST endpoints (blocked at the Gateway from public access).

## Known Simplifications (vs. the full design doc)

This implementation favors a runnable, correct skeleton over exhaustive coverage
of every test case and infra option described in the original design phases:

- Redis is deployed single-node (not clustered) in the Kubernetes manifests.
- Kafka runs via Confluent images in Docker Compose; the Kubernetes manifests
  don't include a Strimzi operator setup (add separately for production use).
- Contract testing (Pact), load testing (k6), and E2E testing (Playwright)
  configs described in the design phases are not included in this generated
  codebase — see the design conversation for full examples to add back in.
- JaCoCo coverage-gate enforcement is not wired into the POMs in this codebase.

These are reasonable next additions, not implementation bugs — the core
business logic, security model, and event-driven consistency model are
fully implemented and tested. See `COMMANDS.md` for the list of fixes applied
after initial local Docker Compose testing surfaced real build and runtime
issues (Maven not installed in the builder image, nginx.conf build-context
path, MapStruct annotation-processor fragility, gateway public-path gaps).

