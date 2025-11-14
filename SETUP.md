# Quick Setup Guide

This guide explains how to build and run the Pokemon TCG Collection application.

## Prerequisites

- **Docker Desktop** installed and running
- **Maven 3.8+** installed
- **Java 17+** (JDK)
- At least **4GB of available RAM**

## ⚠️ Important: Card Images Setup

**For exam evaluators**: The card images are provided in a separate folder due to their size (not in git repository).

### Setting up images:

1. You should have received a folder named `Scarlet&Violet-Cards` containing 258 `.webp` image files
2. Place this folder in the **same directory** as the project folder:

```
Your-Desktop/
├── PKMN-microservice-exam-main/    ← Project folder
└── Scarlet&Violet-Cards/            ← Image folder (258 .webp files)
```

3. Move the image folder into the project:

```bash
# From the project root directory
mv ../Scarlet\&Violet-Cards .
```

4. Verify images are in place:

```bash
ls Scarlet\&Violet-Cards/*.webp | wc -l
# Should output: 258
```

**Without the images**: The application will work perfectly (all backend functionality), but card images will not display in the frontend.

## Quick Start

### 1. Build All Services

Build the Java services with Maven:

```bash
# Build Catalog Service
cd catalog-service
mvn clean package -DskipTests
cd ..

# Build Collection Service
cd collection-service
mvn clean package -DskipTests
cd ..

# Build API Gateway
cd api-gateway
mvn clean package -DskipTests
cd ..
```

### 2. Start All Services

Start all services with Docker Compose:

```bash
docker compose up -d
```

This will start:
- PostgreSQL databases (catalog-db, collection-db)
- RabbitMQ message broker
- Redis cache
- Consul (service discovery & centralized config)
- Media Service (Python/Flask)
- Catalog Service (Spring Boot)
- Collection Service (Spring Boot)
- API Gateway (Spring Cloud Gateway)
- Prometheus & Grafana (monitoring)

### 3. Wait for Services to Start

Wait 30-60 seconds for all services to become healthy:

```bash
# Check status
docker compose ps

# Watch logs
docker compose logs -f
```

### 4. Access the Application

Once all services show `(healthy)` status:

- **Frontend**: Open `frontend/index.html` in your browser
- **API Gateway**: http://localhost:8080
- **Consul UI**: http://localhost:8500 (service discovery)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

## Verify Everything Works

Test the API Gateway:

```bash
# Check health
curl http://localhost:8080/actuator/health

# Get all cards (should return 258 cards)
curl http://localhost:8080/api/catalog/sets/SV01/cards | jq '. | length'

# Check Consul services
curl http://localhost:8500/v1/catalog/services | jq
```

Expected output:
- Health check returns `{"status":"UP"}`
- Cards query returns `258`
- Consul shows: `api-gateway`, `catalog-service`, `collection-service`

## Service Ports

| Service | Port(s) | Purpose |
|---------|---------|---------|
| API Gateway | 8080 | Main entry point |
| Catalog Service | 8081 | Card catalog (internal) |
| Collection Service | 8082 | User collections |
| Media Service | 8084 | Card images |
| Consul | 8500, 8600 | Service discovery |
| PostgreSQL (Catalog) | 5432 | Catalog database |
| PostgreSQL (Collection) | 5433 | Collection database |
| RabbitMQ | 5672, 15672 | Message broker |
| Redis | 6379 | Cache |
| Prometheus | 9090 | Metrics |
| Grafana | 3000 | Dashboards |

## Architecture

```
Frontend (HTML/CSS/JS)
    ↓ HTTP
API Gateway :8080
    ↓
    ├─→ Catalog Service :8081 (PostgreSQL 5432)
    ├─→ Collection Service :8082 (PostgreSQL 5433)
    └─→ Media Service :8084 (Card images)

Collection Service → RabbitMQ → Catalog Service (async events)
All Services → Consul (service discovery & config)
```

## Common Operations

### Stop All Services

```bash
docker compose down
```

### Stop and Remove All Data

```bash
docker compose down -v
```

### Restart a Specific Service

```bash
docker compose restart catalog-service
```

### View Service Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f catalog-service
```

### Rebuild After Code Changes

```bash
# Rebuild specific service
cd catalog-service
mvn clean package -DskipTests
cd ..
docker compose up -d --build catalog-service

# Or rebuild all
mvn clean package -DskipTests
docker compose up -d --build
```

### Scale Catalog Service (Load Balancing)

```bash
# Scale to 3 instances
docker compose up -d --scale catalog-service=3

# Verify
docker ps --filter "name=catalog-service"
```

## Troubleshooting

### Services Won't Start

```bash
# Check if ports are already in use
lsof -i :8080  # API Gateway
lsof -i :5432  # PostgreSQL

# Kill process using port
kill -9 <PID>

# Restart services
docker compose down
docker compose up -d
```

### Database Connection Errors

```bash
# Ensure databases start first
docker compose up -d catalog-db collection-db
sleep 15
docker compose up -d
```

### "Consul not healthy" Errors

```bash
# Check Consul logs
docker compose logs consul

# Restart Consul
docker compose restart consul
docker compose up -d
```

### Images Not Loading

The media service has all 258 images baked into the Docker image. If images aren't loading:

```bash
# Check media service
docker compose logs media-service

# Should see: "Found 258 images"
docker compose exec media-service ls -la /app/images/ | wc -l
```

### Fresh Start

If something is broken, start completely fresh:

```bash
# Stop everything and remove volumes
docker compose down -v

# Rebuild services
mvn clean package -DskipTests

# Start fresh
docker compose up -d

# Wait 60 seconds
sleep 60

# Verify
docker compose ps
curl http://localhost:8080/actuator/health
```

## What Makes This Grade A

The project meets all exam requirements including:

✅ **Krav 1-3 (Grade E)**: Multiple services, synchronous REST, asynchronous RabbitMQ
✅ **Krav 4-6 (Grade D)**: Clear structure, documented architecture, Docker containers
✅ **Krav 7-8 (Grade C)**: API Gateway with load balancing
✅ **Krav 9-10 (Grade B)**: Health checks, Docker Compose orchestration
✅ **Krav 11-12 (Grade A)**: Consul centralized config, scalable services

### Consul (Grade A Requirement #11)

Consul provides centralized configuration and service discovery:

- **Service Registry**: All services auto-register with Consul on startup
- **Health Monitoring**: Consul monitors each service via `/actuator/health`
- **Service Discovery**: Services can find each other through Consul DNS
- **Centralized Config**: Configuration in `/consul-config/*.json`

Access Consul UI at http://localhost:8500 to see all registered services.

### Load Balancing (Grade A Requirement #12)

The Catalog Service supports horizontal scaling:

```bash
# Scale to 3 instances
docker compose up -d --scale catalog-service=3
```

The API Gateway automatically load balances requests across all healthy instances using Spring Cloud LoadBalancer with round-robin distribution.

## Additional Resources

- **Full Documentation**: See `README.md` for complete architecture details
- **Reflections**: See `REFLECTION.md` for development insights
- **API Examples**: See `README.md` section "Testing the Application"

---

**Questions?** Check the main README.md or contact Mathias.Alsos03@gmail.com
