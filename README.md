# PKMN/OP Binder - Digital TCG Collection Manager

A microservices-based web application for managing Trading Card Game (TCG) collections, with a focus on Pokémon TCG.

**Student:** Mathias Alsos Paulsen
**Course:** PG3402 Microservices
**Date:** December 2024
**Technology Stack:** Java 17+, Spring Boot 3.2.0, Spring Cloud 2023.0.0, PostgreSQL, RabbitMQ, Docker Compose

---

## Project Overview

PKMN/OP Binder allows collectors to:
- Build digital "binders" for their TCG collections
- Track card ownership and see what's missing per set
- Search and filter cards by name, set, number, rarity, and variant
- Share trade/wish lists with friends via public, read-only links (planned)
- View high-quality card images

The application uses microservices architecture to separate concerns:
- **Catalog Service**: Manages card and set metadata (read model)
- **Collection Service**: Handles user card collections (write model)
- **Share Service**: Generates shareable public links (planned)
- **Media Service**: Serves card images via HTTP
- **API Gateway**: Single entry point with routing and rate limiting
- **Message Broker**: RabbitMQ for asynchronous event-driven communication

---

## Architecture Diagram

### High-Level Architecture

```
                    ┌─────────────────┐
                    │  API Gateway    │  ← Single entry point
                    │  (Port 8080)    │    (routing, rate limiting)
                    └────────┬────────┘
                             │
             ┌───────────────┼────────────────┐
             │               │                │
             ▼               ▼                ▼
    ┌─────────────┐  ┌─────────────┐  ┌──────────────┐
    │  Catalog    │  │ Collection  │  │Media Service │
    │  Service    │  │  Service    │  │(Python Flask)│
    │(Port 8081)  │  │(Port 8082)  │  │(Port 8084)   │
    └──────┬──────┘  └──────┬──────┘  └──────────────┘
           │                │
           │                │ Publishes events
           │                ├─────────────────┐
           │                │                 │
           ▼                ▼                 ▼
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │ PostgreSQL  │  │ PostgreSQL  │  │  RabbitMQ   │
    │ catalog_db  │  │collection_db│  │Message Broker│
    │ (Port 5432) │  │ (Port 5433) │  │(Port 5672)  │
    └─────────────┘  └─────────────┘  └──────┬──────┘
                                              │
                                              │ Consumes events
                                              ▼
                                       ┌─────────────┐
                                       │  Catalog    │
                                       │  Service    │
                                       │(Event Listener)
                                       └─────────────┘
```

### Communication Patterns

**Synchronous (REST/HTTP):**
- API Gateway → Services (routing)
- Frontend → API Gateway (user interactions)
- Media Service → Filesystem (image serving)

**Asynchronous (RabbitMQ Events):**
- Collection Service → RabbitMQ (publishes CardAdded/Updated/Removed events)
- RabbitMQ → Catalog Service (consumes events for read model updates)

### Design Patterns

- **API Gateway Pattern**: Single entry point for all client requests
- **Database per Service**: Each service owns its own database
- **Event-Driven Architecture**: Services communicate via domain events
- **CQRS-inspired**: Collection Service (write model) + Catalog Service (read model)

---

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Docker** and **Docker Compose**

### Card Images

**Note**: The `Scarlet&Violet-Cards/` directory containing card images is excluded from git (too large for GitHub).

To run the application with images:
1. Download Pokémon TCG card images for Scarlet & Violet Base Set (SV01)
2. Place images in `Scarlet&Violet-Cards/` directory at project root
3. Images should be named: `001.jpg`, `002.jpg`, etc.

Alternatively, the application works without images - cards will show placeholder icons.

---

## Build Instructions

### 1. Build all services with Maven

From the project root directory:

```bash
mvn clean package -DskipTests
```

This will:
- Compile all Java source code
- Run unit tests (unless skipped)
- Package each service as a JAR file in their respective `target/` directories

### 2. Build Docker images

After Maven build completes, build Docker images:

```bash
docker compose build
```

---

## Run Instructions

### Option 1: Using Docker Compose (Recommended)

Start all services with a single command:

```bash
docker compose up -d
```

This will start:
- PostgreSQL databases (catalog, collection, share)
- RabbitMQ message broker
- MinIO object storage
- Redis (for rate limiting)
- API Gateway (port 8080)
- Catalog Service (port 8081)
- Prometheus (port 9090)
- Grafana (port 3000)

**Wait 30-60 seconds** for all services to fully initialize and run database migrations.

### Check Service Health

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Catalog Service
curl http://localhost:8081/actuator/health
```

### Option 2: Running Services Locally (Development)

If you want to run services individually:

1. **Start infrastructure:**
   ```bash
   docker compose up -d catalog-db rabbitmq minio redis
   ```

2. **Run Catalog Service:**
   ```bash
   cd catalog-service
   mvn spring-boot:run
   ```

3. **Run API Gateway:**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

---

## Consul Configuration (Exam Krav 11 - Grade A)

The application uses **Consul** for centralized configuration management and service discovery.

### Overview

Consul provides:
- **Centralized Config**: All service configuration in one place
- **Service Discovery**: Services automatically register and discover each other
- **Health Checking**: Consul monitors service health
- **Dynamic Updates**: Configuration changes without redeploying

### Architecture

```
┌─────────────────────────────────────────────┐
│          Consul Server (port 8500)          │
│  - Configuration Storage                    │
│  - Service Registry                         │
│  - Health Monitoring                        │
└──────────┬──────────────────────────────────┘
           │
           ├─── Registers ───> API Gateway
           ├─── Registers ───> Catalog Service (x3 instances)
           └─── Registers ───> Collection Service
```

### Configuration Files

Configuration is stored in [`consul-config/`](consul-config/) directory:

- [`catalog-service.json`](consul-config/catalog-service.json) - Catalog service config
- [`collection-service.json`](consul-config/collection-service.json) - Collection service config
- [`api-gateway.json`](consul-config/api-gateway.json) - API Gateway config

### Accessing Consul

**Consul Web UI**: http://localhost:8500

From the UI you can:
- View all registered services
- Check service health status
- Browse configuration key/value store
- Monitor service instances

### How It Works

1. **Services Start**: Each service reads `bootstrap.yml` which points to Consul
2. **Fetch Config**: Services fetch their configuration from Consul on startup
3. **Register**: Services register themselves with Consul
4. **Health Checks**: Consul periodically checks `/actuator/health` endpoints
5. **Discovery**: Services can discover each other through Consul DNS/API

### Example: Adding New Configuration

To add a new config value:

```bash
# Via Consul CLI
consul kv put config/catalog-service/data '{"new-property": "value"}'

# Via Web UI
# Navigate to Key/Value → config/catalog-service/data → Edit
```

Services will pick up changes on restart (or with Spring Cloud Config refresh endpoint).

### Benefits

- **Single Source of Truth**: All config in Consul, not scattered across files
- **Environment-Specific**: Different configs for dev/staging/prod
- **No Rebuilds**: Change config without rebuilding Docker images
- **Auditability**: Consul tracks all config changes
- **Service Discovery**: No hardcoded service URLs

---

## Load Balancing (Exam Krav 8)

The application supports **horizontal scaling** of the Catalog Service with automatic load balancing through Spring Cloud LoadBalancer.

### How It Works

1. **Spring Cloud Gateway** uses the `lb://` URI scheme to enable client-side load balancing
2. **Docker DNS** resolves the service name to all container IPs when scaled
3. **Round-robin distribution** spreads requests evenly across all instances
4. **Health checks** ensure only healthy instances receive traffic

### Scaling Catalog Service

To run multiple instances of the Catalog Service:

```bash
# Scale to 3 instances
docker compose up -d --scale catalog-service=3

# Verify instances are running
docker ps --filter "name=catalog-service"
```

You should see output similar to:
```
CONTAINER ID   IMAGE                                       PORTS      NAMES
9442c7343d15   mikrotjenestereksamen2025-catalog-service   8081/tcp   mikrotjenestereksamen2025-catalog-service-1
141def61cfa0   mikrotjenestereksamen2025-catalog-service   8081/tcp   mikrotjenestereksamen2025-catalog-service-2
8b56e3d4fad0   mikrotjenestereksamen2025-catalog-service   8081/tcp   mikrotjenestereksamen2025-catalog-service-3
```

### Testing Load Balancing

Run the included test script:

```bash
./test-load-balancing.sh
```

This will:
1. Check running catalog-service instances
2. Send 20 test requests through the API Gateway
3. Display request distribution across instances

**Expected output:**
```
Request distribution across instances:
   - catalog-service-1: 7 requests
   - catalog-service-2: 6 requests
   - catalog-service-3: 7 requests
```

### Manual Testing

Send multiple requests and observe distribution:

```bash
# Send 10 requests
for i in {1..10}; do
  curl -s http://localhost:8080/api/catalog/sets > /dev/null
  echo "Request $i sent"
done

# Check which instances handled requests
docker logs mikrotjenestereksamen2025-catalog-service-1 2>&1 | grep "GET.*sets" | wc -l
docker logs mikrotjenestereksamen2025-catalog-service-2 2>&1 | grep "GET.*sets" | wc -l
docker logs mikrotjenestereksamen2025-catalog-service-3 2>&1 | grep "GET.*sets" | wc -l
```

### Configuration Details

**API Gateway** ([GatewayConfig.java](api-gateway/src/main/java/no/kristiania/pg3402/gateway/config/GatewayConfig.java)):
```java
.route("catalog-service", r -> r
    .path("/api/catalog/**")
    .uri("lb://catalog-service"))  // lb:// enables load balancing
```

**Docker Compose** ([docker-compose.yml](docker-compose.yml)):
```yaml
catalog-service:
  # No container_name to allow multiple instances
  expose:
    - "8081"  # Exposed internally, not mapped to host
  # Can be scaled with --scale flag
```

### Benefits

- **High availability**: If one instance fails, others continue serving traffic
- **Better performance**: Multiple instances can handle more concurrent requests
- **Zero downtime**: Can update instances one at a time (rolling updates)
- **Resource optimization**: Scale up during peak times, scale down when idle

---

## Testing the Application

### User Stories & Test Scenarios

#### User Story 1: Registrere og spore kort

**Som samler vil jeg legge til kort i min digitale binder (ved å søke opp sett/nummer), slik at jeg har oversikt over hva jeg eier og hva som mangler.**

**Acceptance Criteria:**
- Søke etter kort i katalog
- Legge til kort i min samling med antall, tilstand, og variant
- Se oversikt over eide kort
- Få tilbakemelding når kort legges til

**Test Scenario:**

```bash
# 1. Browse available cards in a set
curl http://localhost:8080/api/catalog/sets/SV01/cards

# 2. Add a card to user's collection
curl -X POST http://localhost:8082/api/collections/users/1/cards \
  -H "Content-Type: application/json" \
  -d '{
    "cardId": 10,
    "quantity": 2,
    "condition": "NEAR_MINT",
    "isReverseHolo": false,
    "notes": "Pulled from booster pack"
  }'

# 3. View user's collection
curl http://localhost:8082/api/collections/users/1/cards

# 4. Update card quantity
curl -X PUT http://localhost:8082/api/collections/users/1/cards/10 \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3,
    "condition": "NEAR_MINT"
  }'

# 5. Remove card from collection
curl -X DELETE http://localhost:8082/api/collections/users/1/cards/10
```

**Expected Events:** CardAddedEvent, CardUpdatedEvent, CardRemovedEvent published to RabbitMQ and consumed by Catalog Service.

#### User Story 2: Se progresjon per sett

**Som samler vil jeg se antall eide/manglende kort i et valgt sett (med filter på rarity/variant), slik at jeg kan planlegge kjøp/bytte.**

**Acceptance Criteria:**
- Vise totalt antall kort i sett
- Vise hvor mange kort jeg eier
- Vise prosentvis fullføring
- Liste over manglende kort

**Test Scenario:**

```bash
# 1. Get set overview
curl http://localhost:8080/api/catalog/sets/SV01

# 2. Get all cards in set
curl http://localhost:8080/api/catalog/sets/SV01/cards

# 3. Get user's collection for this set
curl http://localhost:8082/api/collections/users/1/cards?setCode=SV01

# 4. Calculate completion percentage (client-side for MVP)
# Example: User owns 50 out of 198 cards = 25.3% completion
```

**Implementation Status:** Partially implemented. Basic card tracking exists. Aggregation/progression calculation needs to be added to Collection Service.

#### User Story 3: Dele «trade/wish»-liste

**Som samler vil jeg kunne dele en offentlig lenke med kort jeg ønsker/bytter bort, slik at venner enkelt kan vurdere bytte.**

**Acceptance Criteria:**
- Generere unik, offentlig lenke
- Lenken viser valgte kort uten sensitive data
- Lenken er read-only
- Kan oppdatere/deaktivere delte lister

**Test Scenario:**

```bash
# 1. Create a shareable wish list (planned feature)
curl -X POST http://localhost:8083/api/shares/users/1/wishlists \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Missing SV01 Rares",
    "cardIds": [15, 23, 42, 67],
    "listType": "WISH"
  }'

# 2. Access public share link (planned feature)
curl http://localhost:8083/api/shares/public/{shareToken}
```

**Implementation Status:** Not implemented. Share Service needs to be created (see TODO).

### Additional Test Endpoints

#### Direct Service Access (Bypass Gateway)

```bash
# Catalog Service direct access
curl http://localhost:8081/health
curl http://localhost:8081/sets

# Metrics endpoint
curl http://localhost:8081/actuator/prometheus
```

---

## Monitoring & Observability

### Prometheus Metrics

Access Prometheus UI: [http://localhost:9090](http://localhost:9090)

Sample queries:
- `http_server_requests_seconds_count` - Request count
- `jvm_memory_used_bytes` - Memory usage
- `system_cpu_usage` - CPU usage

### Grafana Dashboards

Access Grafana: [http://localhost:3000](http://localhost:3000)

**Default credentials:**
- Username: `admin`
- Password: `admin`

### Logs

View service logs:

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f catalog-service
docker compose logs -f api-gateway
```

---

## Infrastructure Components

### RabbitMQ Management UI

- URL: [http://localhost:15672](http://localhost:15672)
- Username: `guest`
- Password: `guest`

### MinIO Console

- URL: [http://localhost:9001](http://localhost:9001)
- Username: `minioadmin`
- Password: `minioadmin`

### PostgreSQL Databases

Connect using any PostgreSQL client:

**Catalog DB:**
- Host: `localhost`
- Port: `5432`
- Database: `catalog_db`
- User: `postgres`
- Password: `postgres`

**Collection DB:**
- Host: `localhost`
- Port: `5433`
- Database: `collection_db`
- User: `postgres`
- Password: `postgres`

**Share DB:**
- Host: `localhost`
- Port: `5434`
- Database: `share_db`
- User: `postgres`
- Password: `postgres`

---

## Stopping the Application

```bash
# Stop all services
docker compose down

# Stop and remove volumes (deletes all data)
docker compose down -v
```

---

## Project Structure

```
pkmn-op-binder/
├── api-gateway/              # API Gateway service
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── catalog-service/          # Catalog service
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │           ├── application.yml
│   │           └── db/migration/    # Flyway migrations
│   ├── Dockerfile
│   └── pom.xml
├── collection-service/       # (To be implemented)
├── share-service/           # (To be implemented)
├── media-service/           # (To be implemented)
├── monitoring/
│   └── prometheus.yml       # Prometheus configuration
├── docker-compose.yml       # Docker orchestration
├── pom.xml                  # Parent POM
└── README.md
```

---

## Requirements Checklist

### Arbeidskrav - Funksjonelle Krav

- [x] **K1 - Katalog (Pokémon MVP):** Hente/lagre metadata om sett og kort
  - Implementation: [catalog-service](catalog-service/)
  - 258 cards from SV01 (Scarlet & Violet) set loaded via Flyway
  - REST API: `/api/catalog/sets`, `/api/catalog/cards`

- [x] **K2 - Samling:** CRUD på brukerens eide kort
  - Implementation: [collection-service](collection-service/)
  - Tracks quantity, condition, variant, notes, timestamps
  - REST API: `/api/collections/users/{userId}/cards`

- [x] **K3 - Progresjon:** Aggregater per sett (partially implemented)
  - Basic card tracking exists
  - Client can calculate completion by comparing catalog vs collection
  - **TODO**: Add server-side aggregation endpoint

- [ ] **K4 - Deling:** Generere delbare lenker
  - **Status**: Not implemented
  - **TODO**: Create Share Service (planned)

- [x] **K5 - Søk/Filter:** Navn/sett/nummer/rarity/variant
  - Implementation: Catalog Service
  - Query parameters: `name`, `setCode`, `rarity`, `cardNumber`

- [ ] **K6 - Auth:** OIDC (Google) med per-bruker data
  - **Status**: Dependencies configured but not enforced
  - **TODO**: Activate OAuth2/OIDC security configuration

- [x] **K7 - Hendelser:** Publiser domenehendelser
  - Implementation: RabbitMQ integration
  - Events: `CardAddedEvent`, `CardUpdatedEvent`, `CardRemovedEvent`
  - Publisher: Collection Service
  - Consumer: Catalog Service (logs events)

- [x] **K8 - Media (MVP-light):** Brukerbilder på kort
  - Implementation: [media-service](media-service/) (Python Flask)
  - Serves 258 card images from filesystem
  - HTTP endpoint: `http://localhost:8084/images/{filename}`

### Exam Requirements (from 2025-10-pg3402-exam.pdf)

#### Grade E Requirements (CRITICAL - Must Pass)
- [x] **Krav 1:** Multiple services with different functionality
  - 4 operational services: Catalog, Collection, Media, API Gateway

- [x] **Krav 2:** Synchronous communication (REST/HTTP)
  - API Gateway routes to backend services
  - Frontend communicates with Gateway

- [x] **Krav 3:** Asynchronous communication (RabbitMQ) **✅ IMPLEMENTED**
  - RabbitMQ broker with TopicExchange
  - Collection Service publishes domain events
  - Catalog Service consumes events via @RabbitListener
  - Tested and working (see logs)

#### Grade D Requirements
- [x] **Krav 4:** Clear service structure and functionality
  - Each service has single responsibility
  - Clean package structure with controllers, services, repositories

- [x] **Krav 5:** Architecture consistent with documentation
  - README matches actual implementation
  - Architecture diagram shows sync + async communication

- [x] **Krav 6:** Docker container deployment
  - All services containerized with Dockerfiles
  - Multi-stage builds for Java services

#### Grade C Requirements
- [x] **Krav 7:** API Gateway for routing
  - Spring Cloud Gateway implementation
  - Routes: `/api/catalog/**`, `/api/collections/**`
  - Rate limiting with Redis

- [x] **Krav 8:** Load balancing - Spring Cloud LoadBalancer with scalable catalog-service (see "Load Balancing" section)
  - Catalog Service can be scaled to multiple instances with `--scale catalog-service=N`
  - API Gateway uses `lb://catalog-service` URI for client-side load balancing
  - Round-robin distribution across healthy instances

#### Grade B Requirements
- [x] **Krav 9:** Health checks via Actuator
  - All Spring Boot services expose `/actuator/health`
  - Docker healthchecks configured in docker-compose.yml

- [x] **Krav 10:** Docker Compose orchestration
  - Complete docker-compose.yml with all services
  - Health check dependencies
  - Volume management for databases

#### Grade A Requirements
- [x] **Krav 11:** Centralized configuration (Consul) - ✅ IMPLEMENTED
  - Consul server running on port 8500 with Web UI
  - All Spring Boot services use Spring Cloud Consul Config
  - Configuration stored in `/consul-config/*.json` files
  - Services auto-register with Consul for service discovery
  - See "Consul Configuration" section for details

- [x] **Krav 12:** Multiple service instances
  - **Status**: ✅ IMPLEMENTED - Catalog Service supports horizontal scaling
  - Scalable with `docker compose up -d --scale catalog-service=N`
  - Load balanced via Spring Cloud LoadBalancer (see "Load Balancing" section)

---

## Architecture Decisions & Simplifications

### Key Design Decisions

#### 1. Event-Driven Architecture with RabbitMQ
**Decision:** Use RabbitMQ for asynchronous communication between services.

**Rationale:**
- Decouples Collection Service (write model) from Catalog Service (read model)
- Enables future features like notifications, search indexing, analytics
- Satisfies Exam Krav 3 (critical requirement)

**Trade-offs:**
- Added complexity with message broker infrastructure
- Eventual consistency between services
- Need to handle message serialization (solved with Jackson JavaTimeModule)

#### 2. Database per Service Pattern
**Decision:** Each service has its own PostgreSQL database.

**Rationale:**
- True service independence (can deploy/scale separately)
- No shared database coupling
- Each service owns its data model
- Follows microservices best practices

**Implementation:**
- `catalog_db` (Port 5432): Card and set metadata
- `collection_db` (Port 5433): User card collections
- Future: `share_db` (Port 5434): Shareable links

#### 3. CQRS-Inspired Design
**Decision:** Separate write model (Collection Service) from read model (Catalog Service).

**Rationale:**
- Collection Service handles user mutations (add/update/remove cards)
- Catalog Service optimized for queries (search, filter, browse)
- Allows independent scaling based on read vs write load
- Events keep read model eventually consistent

**Not Full CQRS:**
- Not using separate read/write databases per service
- No event sourcing (events are notifications, not source of truth)
- Simplified for MVP scope

#### 4. API Gateway as Single Entry Point
**Decision:** Spring Cloud Gateway for all external traffic.

**Rationale:**
- Single public endpoint for frontend
- Centralized cross-cutting concerns (rate limiting, CORS)
- Service discovery and routing
- Future: Authentication/authorization enforcement

**Configuration:**
- Rate limiting: 100 requests/minute per IP (Redis-based)
- Routes to backend services with path rewriting
- Health checks for circuit breaker pattern

#### 5. Python Flask for Media Service
**Decision:** Use Python instead of Java for image serving.

**Rationale:**
- Demonstrates polyglot microservices
- Simpler for static file serving
- Lightweight compared to Spring Boot
- Shows service independence (different tech stack OK)

**Trade-offs:**
- No Spring Actuator metrics
- Different deployment pattern
- Less consistency in codebase

#### 6. Flyway for Database Migrations
**Decision:** Use Flyway for versioned database schema management.

**Rationale:**
- Repeatable, version-controlled schema changes
- Automatic migration on service startup
- Seed data for development (258 cards loaded via V3 migration)
- Production-ready approach

#### 7. No Authentication Enforcement (MVP)
**Decision:** OAuth2/OIDC dependencies configured but not enforced.

**Rationale:**
- Simplifies testing and demonstration
- Infrastructure ready for future activation
- Focus on microservices patterns first

**Security Implications:**
- NOT production-ready
- User ID passed as path parameter (no validation)
- Should activate before any real deployment

### Simplifications from Original Plan

#### 1. No Share Service (Yet)
**Original Plan:** Separate service for shareable links (User Story 3).

**Current Status:** Not implemented.

**Justification:**
- Focused on core functionality (Krav 1-3 for passing grade)
- User Stories 1-2 demonstrate microservices principles
- Can be added without major refactoring

#### 2. No Search Service
**Original Plan:** Dedicated OpenSearch/Elasticsearch service.

**Current Status:** Basic filtering via SQL queries in Catalog Service.

**Justification:**
- MVP dataset (258 cards) small enough for SQL
- Adds significant infrastructure complexity
- Catalog Service handles search adequately for demo

#### 3. No MinIO Object Storage
**Original Plan:** MinIO S3-compatible storage for images.

**Current Status:** Images served directly from filesystem.

**Justification:**
- Simplifies media service implementation
- Static card images don't require object storage
- Filesystem adequate for demo purposes

#### 4. Single Instance per Service
**Original Plan:** Multiple instances with load balancing (Krav 12).

**Current Status:** One instance per service.

**Justification:**
- Infrastructure ready (Docker Compose can scale)
- Gateway routing configured
- Prioritized passing requirements over A-grade features

#### 5. No Centralized Config (Consul)
**Original Plan:** Consul for distributed configuration (Krav 11).

**Current Status:** Environment variables in docker-compose.yml.

**Justification:**
- Simpler for demo environment
- Each service has application.yml
- Config externalized via environment variables

### Technology Choices

| Component | Technology | Version | Justification |
|-----------|-----------|---------|---------------|
| API Gateway | Spring Cloud Gateway | 4.1.0 | Industry standard, reactive, Spring ecosystem |
| Services | Spring Boot | 3.2.0 | Modern Java framework, extensive ecosystem |
| Persistence | PostgreSQL | 15.14 | Relational data, ACID compliance, mature |
| Message Broker | RabbitMQ | 3.13 | Reliable messaging, easy to operate |
| Migrations | Flyway | 10.4.1 | Version control for databases |
| Monitoring | Prometheus + Grafana | Latest | Industry standard metrics/dashboards |
| Containerization | Docker + Compose | Latest | Development parity with production |
| Media Service | Python Flask | 3.9+ | Lightweight, simple for static serving |

---

## Test Credentials

All services use default development credentials (see Infrastructure Components section above).

**Note:** These are for development/testing only. Never use default credentials in production.

---

## Known Issues & Limitations

### Current Limitations

1. **Authentication Not Enforced**
   - OAuth2/OIDC configured but disabled for testing
   - User ID passed as URL parameter (no validation)
   - NOT production-ready

2. **Share Service Not Implemented**
   - User Story 3 (shareable links) not available
   - Planned but deprioritized for MVP

3. **Server-Side Progression Aggregates**
   - Client must calculate completion percentage
   - Missing endpoint: `/api/collections/users/{id}/sets/{setCode}/progress`

4. **Single Service Instances**
   - No load balancing demonstration
   - Docker Compose can scale but not configured

5. **No Centralized Configuration**
   - Each service has own application.yml
   - Environment variables in docker-compose.yml
   - No Consul integration

6. **Event Handling is Logging Only**
   - Catalog Service consumes events but only logs them
   - No actual read model updates from events
   - Future: Update card popularity, trending, etc.

### Known Issues

- **Media Service Metrics**: Python Flask service doesn't expose Prometheus metrics
- **CORS Configuration**: Removed duplicate CORS configs, only in Gateway
- **Error Handling**: Basic error responses, could add more detailed error codes

---

## Future Enhancements

### High Priority
1. **Activate OAuth2/OIDC Authentication**
   - Enforce security in Gateway
   - User context propagation to services
   - Token validation

2. **Implement Share Service**
   - Generate unique share tokens
   - Public read-only links
   - Track view counts

3. **Server-Side Progression**
   - Aggregate endpoint for set completion
   - Missing cards list
   - Rarity breakdown

### Medium Priority
4. **Multiple Service Instances**
   - Scale with `docker compose up --scale catalog-service=3`
   - Demonstrate load balancing
   - Service discovery

5. **Enhanced Event Handling**
   - Update Catalog Service with actual logic
   - Track card popularity
   - User activity analytics

### Low Priority (Best-Case)
6. **One Piece TCG Support**
   - Multi-game catalog
   - Game type filtering
   - Separate set structures

7. **Dedicated Search Service**
   - OpenSearch/Elasticsearch
   - Advanced text search
   - Faceted filtering

8. **Centralized Configuration**
   - Consul or Spring Cloud Config
   - Dynamic configuration updates
   - Feature flags

---

## Troubleshooting

### Services won't start

1. Check if ports are already in use:
   ```bash
   lsof -i :8080  # API Gateway
   lsof -i :5432  # PostgreSQL
   ```

2. Check Docker logs:
   ```bash
   docker compose logs
   ```

3. Restart services:
   ```bash
   docker compose restart
   ```

### Database connection issues

Ensure databases are healthy before starting services:
```bash
docker compose up -d catalog-db collection-db share-db
sleep 10  # Wait for DBs to initialize
docker compose up -d
```

---

## Contact

**Student:** Mathias Alsos Paulsen
**Course:** PG3402 Microservices
**Date:** December 2024
