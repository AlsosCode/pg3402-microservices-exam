# Pokemon Binder - Digital TCG Collection Manager

A modern microservices-based web application for managing your Pokémon Trading Card Game collection with real-time updates, beautiful card imagery, and progress tracking.

**Live Demo**: [https://pg3402-microservices-exam.vercel.app/]

![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)
![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?logo=springboot)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Events-FF6600?logo=rabbitmq)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql)

**Student:** Mathias Alsos Paulsen 
**Course:** PG3402 Microservices 
**Date:** November 2025

---

## ✨ Features

- **Digital Binder**: Build your personal TCG collection with 258 cards from Scarlet & Violet Base Set
- **Progress Tracking**: See your collection completion percentage (Standard/Full/Complete sets)
- **Search & Filter**: Find cards by name, number, rarity, and type
- **Real-Time Updates**: Event-driven architecture with RabbitMQ keeps everything in sync
- **Beautiful Card Display**: High-quality card images with hover effects
- **Dark Mode**: Toggle between light and dark themes with persistent preference
- **Add/Remove Cards**: Manage your collection with intuitive UI and instant feedback
- **Collection Badge**: Cards you own show a checkmark badge in the catalog
- **Fully Responsive**: Works seamlessly on desktop, tablet, and mobile

## 🎯 Quick Start

### Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Docker** and **Docker Compose**

### Running Locally

1. **Clone the repository**:
   ```bash
   git clone https://github.com/AlsosCode/pg3402-microservices-exam.git
   cd pg3402-microservices-exam
   ```

2. **Build all services**:
   ```bash
   mvn clean package -DskipTests
   ```

3. **Start with Docker Compose**:
   ```bash
   docker compose up -d
   ```

4. **Start the frontend**:
   ```bash
   cd frontend
   python3 -m http.server 3001
   ```

5. **Open your browser**:
   Visit `http://localhost:3001`

**Wait 30-60 seconds** for all services to initialize before using the app.

### Check Service Health

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Catalog Service
curl http://localhost:8081/actuator/health

# Collection Service
curl http://localhost:8082/actuator/health
```

## 🎨 Usage

### Basic Operations

- **Browse Catalog**: View all 258 cards from Scarlet & Violet Base Set
- **Search**: Type in the search bar to find cards by name or number
- **Filter by Rarity**: Use the dropdown to show only Common, Uncommon, or Rare cards
- **View Card Details**: Click any card to see full details and artwork
- **Add to Collection**: Click the "Add to Collection" button in the card modal
- **Remove from Collection**: Click the "Remove from Collection" button (trash icon) for cards you own
- **Track Progress**: Switch to the Progress tab to see your completion stats
- **Dark Mode**: Click the moon/sun icon in the header to toggle themes

### Frontend Features

- **Collection Badge**: Cards in your collection show a green checkmark
- **Quantity Badge**: In your collection view, see how many copies you own
- **Instant Updates**: Adding or removing cards updates all views immediately
- **Persistent Theme**: Your dark/light mode preference saves to localStorage
- **Loading States**: Smooth loading animations while data fetches

## 🏗️ Architecture

### High-Level Overview

```
                    ┌─────────────────┐
                    │  Frontend       │  ← Vanilla JS (Port 3001)
                    │  (Vercel)       │    Dark mode, Search, Filters
                    └────────┬────────┘
                             │ HTTP
                             ▼
                    ┌─────────────────┐
                    │  API Gateway    │  ← Single entry point
                    │  (Port 8080)    │    Rate limiting (Redis)
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
- Frontend → API Gateway → Backend Services
- Media Service → Filesystem (card images)

**Asynchronous (RabbitMQ Events):**
- Collection Service publishes: `CardAdded`, `CardUpdated`, `CardRemoved` events
- Catalog Service consumes events for read model updates

### Design Patterns

- **API Gateway Pattern**: Single entry point with routing and rate limiting
- **Database per Service**: Each microservice owns its own database
- **Event-Driven Architecture**: Services communicate via domain events (CQRS-inspired)
- **Polyglot Microservices**: Java (Spring Boot) + Python (Flask)

## 🛠️ Technologies Used

### Backend
- **Spring Boot 3.2.0**: Modern Java framework with extensive ecosystem
- **Spring Cloud Gateway**: Reactive API Gateway with load balancing
- **Spring AMQP**: RabbitMQ integration for event-driven messaging
- **PostgreSQL 15**: Reliable relational database with ACID compliance
- **RabbitMQ 3.13**: Message broker for asynchronous communication
- **Redis**: Rate limiting and caching
- **Flyway**: Database migrations with version control
- **Consul**: Centralized configuration and service discovery

### Frontend
- **Vanilla JavaScript**: No framework overhead, fast and simple
- **CSS Custom Properties**: Theme variables for dark mode
- **Font Awesome 6.4.0**: Beautiful, scalable icons
- **Fetch API**: Modern HTTP client for REST calls

### DevOps
- **Docker & Docker Compose**: Containerization and orchestration
- **Prometheus & Grafana**: Metrics collection and visualization
- **Spring Actuator**: Health checks and application metrics

## 📁 Project Structure

```
pg3402-microservices-exam/
├── api-gateway/              # API Gateway (Spring Cloud Gateway)
│   ├── src/
│   │   └── main/
│   │       ├── java/.../gateway/
│   │       │   ├── config/
│   │       │   │   ├── GatewayConfig.java       # Route configuration
│   │       │   │   └── RateLimitConfig.java     # Redis rate limiting
│   │       │   └── ApiGatewayApplication.java
│   │       └── resources/
│   │           └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── catalog-service/          # Catalog Service (Card metadata - Read model)
│   ├── src/
│   │   └── main/
│   │       ├── java/.../catalog/
│   │       │   ├── controller/
│   │       │   │   └── CatalogController.java   # REST endpoints
│   │       │   ├── service/
│   │       │   │   └── CatalogService.java
│   │       │   ├── repository/
│   │       │   │   ├── CardRepository.java
│   │       │   │   └── SetRepository.java
│   │       │   ├── model/
│   │       │   │   ├── Card.java
│   │       │   │   └── Set.java
│   │       │   ├── event/
│   │       │   │   ├── CardAddedEvent.java
│   │       │   │   └── CardEventListener.java   # RabbitMQ consumer
│   │       │   └── CatalogServiceApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── db/migration/
│   │               ├── V1__Create_sets_table.sql
│   │               ├── V2__Create_cards_table.sql
│   │               └── V3__Insert_sv01_cards.sql # 258 cards seed data
│   ├── Dockerfile
│   └── pom.xml
├── collection-service/       # Collection Service (User collections - Write model)
│   ├── src/
│   │   └── main/
│   │       ├── java/.../collection/
│   │       │   ├── controller/
│   │       │   │   └── CollectionController.java # CRUD endpoints
│   │       │   ├── service/
│   │       │   │   └── CollectionService.java
│   │       │   ├── repository/
│   │       │   │   └── UserCardRepository.java
│   │       │   ├── model/
│   │       │   │   └── UserCard.java
│   │       │   ├── event/
│   │       │   │   ├── CardAddedEvent.java
│   │       │   │   └── EventPublisher.java       # RabbitMQ publisher
│   │       │   └── CollectionServiceApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── db/migration/
│   │               └── V1__Create_user_cards_table.sql
│   ├── Dockerfile
│   └── pom.xml
├── media-service/            # Media Service (Python Flask - Image serving)
│   ├── app.py                # Flask application
│   ├── requirements.txt
│   └── Dockerfile
├── frontend/                 # Vanilla JavaScript frontend
│   ├── index.html            # Main HTML (Font Awesome, Dark mode toggle)
│   ├── app.js                # Application logic (Fetch API, LocalStorage)
│   ├── styles.css            # CSS with dark mode variables
│   ├── vercel.json           # Vercel deployment config
│   └── README.md
├── consul-config/            # Consul centralized configuration
│   ├── catalog-service.json
│   ├── collection-service.json
│   └── api-gateway.json
├── monitoring/
│   └── prometheus.yml        # Prometheus scraping config
├── docker-compose.yml        # Multi-container orchestration
├── render.yaml               # Render deployment blueprint
├── DEPLOYMENT.md             # Comprehensive deployment guide
├── DEPLOYMENT-CHECKLIST.md   # Printable deployment checklist
├── REFLECTION.md             # Individual reflections document
├── pom.xml                   # Parent Maven POM
└── README.md                 # This file
```

## 📋 Requirements Checklist

### Exam Requirements (PG3402)

#### Grade E Requirements (CRITICAL - Must Pass ✅)
- ✅ **Krav 1:** Multiple services with different functionality
  - 4 operational services: API Gateway, Catalog, Collection, Media
- ✅ **Krav 2:** Synchronous communication (REST/HTTP)
  - API Gateway routes requests to backend services
  - Frontend communicates with Gateway via Fetch API
- ✅ **Krav 3:** Asynchronous communication (RabbitMQ)
  - Collection Service publishes `CardAdded/Updated/Removed` events
  - Catalog Service consumes events via `@RabbitListener`
  - TopicExchange with routing keys: `card.added`, `card.updated`, `card.removed`

#### Grade D Requirements ✅
- ✅ **Krav 4:** Clear service structure and functionality
  - Each service follows single responsibility principle
  - Clean package structure: controller → service → repository
- ✅ **Krav 5:** Architecture consistent with documentation
  - README architecture diagram matches implementation
  - Documentation includes sync and async communication flows
- ✅ **Krav 6:** Docker container deployment
  - All services containerized with Dockerfiles
  - Multi-stage builds for Java services (Maven build → JRE runtime)

#### Grade C Requirements ✅
- ✅ **Krav 7:** API Gateway for routing
  - Spring Cloud Gateway with route configuration
  - Routes: `/api/catalog/**`, `/api/collections/**`
  - Redis-based rate limiting: 100 requests/minute per IP
- ✅ **Krav 8:** Load balancing
  - Catalog Service supports horizontal scaling: `docker compose up -d --scale catalog-service=3`
  - Spring Cloud LoadBalancer with `lb://` URI scheme
  - Round-robin distribution across healthy instances

#### Grade B Requirements ✅
- ✅ **Krav 9:** Health checks via Actuator
  - All Spring Boot services expose `/actuator/health`
  - Docker healthchecks configured in docker-compose.yml
- ✅ **Krav 10:** Docker Compose orchestration
  - Complete docker-compose.yml with 12 services
  - Dependency management with `depends_on` and health checks
  - Volume persistence for databases

#### Grade A Requirements ✅
- ✅ **Krav 11:** Centralized configuration (Consul)
  - Consul server on port 8500 with Web UI
  - Spring Cloud Consul Config for all services
  - Configuration files in `/consul-config/*.json`
  - Services auto-register for service discovery
- ✅ **Krav 12:** Multiple service instances
  - Catalog Service supports `--scale` flag for horizontal scaling
  - Load balanced via Spring Cloud LoadBalancer (see "Load Balancing" section)

## 🎓 Load Balancing (Krav 8)

The application supports **horizontal scaling** with automatic load balancing.

### How It Works

1. **Spring Cloud Gateway** uses `lb://catalog-service` URI for client-side load balancing
2. **Docker DNS** resolves service name to all container IPs when scaled
3. **Round-robin distribution** spreads requests evenly across instances
4. **Health checks** ensure only healthy instances receive traffic

### Scaling Catalog Service

```bash
# Scale to 3 instances
docker compose up -d --scale catalog-service=3

# Verify instances are running
docker ps --filter "name=catalog-service"
```

### Testing Load Balancing

```bash
# Send 20 requests and see distribution
./test-load-balancing.sh

# Expected output:
# Request distribution:
#   - catalog-service-1: 7 requests
#   - catalog-service-2: 6 requests
#   - catalog-service-3: 7 requests
```

## 🗂️ Consul Configuration (Krav 11 - Grade A)

Consul provides centralized configuration and service discovery.

### Accessing Consul

**Consul Web UI**: http://localhost:8500

From the UI you can:
- View all registered services (api-gateway, catalog-service, collection-service)
- Check service health status
- Browse configuration key/value store
- Monitor service instances

### Configuration Files

Configuration stored in [`consul-config/`](consul-config/):
- [`catalog-service.json`](consul-config/catalog-service.json) - Catalog service config
- [`collection-service.json`](consul-config/collection-service.json) - Collection service config
- [`api-gateway.json`](consul-config/api-gateway.json) - API Gateway config

### Benefits

- **Single Source of Truth**: All config in Consul, not scattered across files
- **Environment-Specific**: Different configs for dev/staging/prod
- **No Rebuilds**: Change config without rebuilding Docker images
- **Service Discovery**: Services find each other via Consul DNS

## 🧪 Testing the Application

### User Story 1: Track Your Collection

```bash
# 1. Browse available cards
curl http://localhost:8080/api/catalog/sets/SV01/cards

# 2. Add a card to your collection
curl -X POST http://localhost:8080/api/collections/users/1/cards \
  -H "Content-Type: application/json" \
  -d '{
    "cardId": 10,
    "quantity": 2,
    "condition": "NEAR_MINT",
    "isReverseHolo": false
  }'

# 3. View your collection
curl http://localhost:8080/api/collections/users/1/cards

# 4. Update quantity
curl -X PUT http://localhost:8080/api/collections/users/1/cards/10 \
  -H "Content-Type: application/json" \
  -d '{"quantity": 3, "condition": "NEAR_MINT"}'

# 5. Remove from collection
curl -X DELETE http://localhost:8080/api/collections/users/1/cards/10
```

### User Story 2: Track Progress

```bash
# Get all cards in set
curl http://localhost:8080/api/catalog/sets/SV01/cards

# Get your collection
curl http://localhost:8080/api/collections/users/1/cards

# Calculate completion percentage (frontend does this)
# Example: 50 out of 198 cards = 25.3% completion
```

## 📊 Monitoring & Observability

### Prometheus Metrics

Access Prometheus: [http://localhost:9090](http://localhost:9090)

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

```bash
# View all logs
docker compose logs -f

# Specific service
docker compose logs -f catalog-service
docker compose logs -f api-gateway
docker compose logs -f collection-service
```

## 🌐 Deployment

For production deployment to Render (backend) and Vercel (frontend), see:
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Comprehensive deployment guide
- **[DEPLOYMENT-CHECKLIST.md](DEPLOYMENT-CHECKLIST.md)** - Printable checklist

**Cost**: $0/month on free tiers ✅

## 🛑 Stopping the Application

```bash
# Stop all services
docker compose down

# Stop and remove volumes (deletes all data)
docker compose down -v
```

## 🗺️ Architecture Decisions

### 1. Event-Driven Architecture (RabbitMQ)

**Decision**: Asynchronous communication between Collection and Catalog services.

**Benefits**:
- Loose coupling between services
- Eventual consistency (Collection writes, Catalog reads)
- Scalable event processing

**Trade-offs**:
- More complex than REST calls
- Need to handle message serialization (Jackson JavaTimeModule)
- Eventual consistency instead of immediate

### 2. Database per Service

**Decision**: Each service owns its database (catalog_db, collection_db).

**Benefits**:
- True service independence
- No shared database coupling
- Independent scaling and deployment

**Trade-offs**:
- No cross-service JOINs
- Need distributed transactions (Saga pattern)
- Data duplication via events

### 3. CQRS-Inspired Design

**Decision**: Separate write model (Collection) from read model (Catalog).

**Benefits**:
- Optimize each service for its use case
- Collection handles writes (add/update/remove)
- Catalog handles reads (search/filter/browse)
- Independent scaling based on load

### 4. Polyglot Microservices

**Decision**: Java (Spring Boot) for business logic, Python (Flask) for media serving.

**Benefits**:
- Use best tool for the job
- Demonstrates service independence
- Lightweight Python for static files

**Trade-offs**:
- Multiple tech stacks to maintain
- Different deployment patterns

## ⚠️ Known Limitations

1. **Authentication Not Enforced**
   - OAuth2/OIDC configured but disabled for demo
   - User ID passed as URL parameter
   - NOT production-ready

2. **Share Service Not Implemented**
   - User Story 3 (shareable links) planned but not built
   - Focused on core requirements (Krav 1-3)

3. **Event Handling is Logging Only**
   - Catalog Service consumes events but only logs them
   - Future: Update card popularity, trending, etc.

## 🚀 Future Enhancements

### High Priority
- **Activate OAuth2/OIDC**: Enforce authentication in API Gateway
- **Implement Share Service**: Generate public shareable links for trade/wish lists
- **Server-Side Progress**: Aggregation endpoint for set completion percentage

### Medium Priority
- **Enhanced Event Logic**: Update Catalog with actual business logic
- **Distributed Tracing**: Add Zipkin/Jaeger for request tracing
- **Contract Testing**: Pact tests between services

### Nice to Have
- **One Piece TCG Support**: Multi-game catalog
- **Search Service**: Elasticsearch for advanced text search
- **Real-Time Notifications**: WebSockets for live updates

## 🐛 Troubleshooting

### Services won't start

```bash
# Check if ports are in use
lsof -i :8080  # API Gateway
lsof -i :5432  # PostgreSQL

# Check Docker logs
docker compose logs

# Restart services
docker compose restart
```

### Database connection issues

```bash
# Ensure databases are healthy first
docker compose up -d catalog-db collection-db
sleep 10  # Wait for initialization
docker compose up -d
```

### RabbitMQ connection issues

```bash
# Access RabbitMQ Management UI
open http://localhost:15672
# Username: guest / Password: guest

# Check queues and exchanges
# Verify services are connected
```

## 📞 Contact

**Mathias Alsos Paulsen**

- Email: Mathias.Alsos03@gmail.com
- GitHub: [@AlsosCode](https://github.com/AlsosCode)
- Course: PG3402 Microservices - Kristiania University College

---

Built with ❤️ using Spring Boot, RabbitMQ, PostgreSQL, and modern microservices architecture. Demonstrates event-driven design, API Gateway pattern, CQRS principles, and horizontal scaling with load balancing.
