# Order Enricher Microservice

## Tech Stack
- **Java 21 & Spring Boot 3** - core application framework
- **Spring Data JPA** - persistence with PostgreSQL
- **Spring Web** - REST API
- **Maven** - dependency management & builds
- **OpenAPI (Swagger)** - API-first design & docs
- **Redis** - caching GET requests
- **Docker & Docker Compose** - containerization & orchestration
- **WireMock** - simulate external Customer and Product microservices

## Setup and Running the Application

This project is fully containerized. Requires Docker and Docker Compose to run the full stack.

#### Prerequisites
- Docker
- Docker Compose (will be included with Docker Desktop)
- Maven (for building the project)
- Java 21

### Running the Service

**1) Clone the repository**
```bash
git clone https://github.com/sreemukha/order-enricher.git
cd order-enricher
```

**2) Build the application JAR**  
From the project root, compile, test, and package:
```bash
mvn clean package
```

**3) Start the entire stack**  
Build the app image and start the app, PostgreSQL, Redis, and WireMock:
```bash
docker-compose up --build
```
`--build` ensures the image is rebuilt if code changed.

**4) Access the application healthcheck**  
[http://localhost:8080/actuator/health]()

## API Usage
Install Bruno and import the collection from the bruno_collection folder in the root directory.

### Example `curl` Commands

**1) Create a new order**
```bash
curl -X POST http://localhost:8080/v1/orders   -H "Content-Type: application/json"   -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-456",
    "productIds": ["PROD-A1", "PROD-B2"],
    "timestamp": "2025-08-15T10:00:00Z"
  }'
```

**2) Get an order by its ID**
```bash
curl http://localhost:8080/v1/orders/ORD-001
```

**3) Get all orders (no filter)**
```bash
curl http://localhost:8080/v1/orders
```

**4) Filter orders by product ID**
```bash
curl "http://localhost:8080/v1/orders?productId=PROD-A1"
```

## Trade-offs and Architectural Decisions

#### 1) Synchronous (Blocking) vs. Asynchronous (Reactive)
**Decision:** Use a traditional synchronous, blocking model (Spring MVC, `RestTemplate`) instead of a reactive stack (WebFlux, `WebClient`).

**Trade-off:**
- **Pros:**
  - Simpler to write, read, and debug.
  - Well-understood one-thread-per-request model
  - Sufficient for non–high-throughput services.
- **Cons:** 
  - Less resource-efficient at very high load due to blocking I/O. For massive concurrency, a reactive approach would be preferable.

#### 2) API-First Design with OpenAPI
**Decision:** Define contract first in `openapi.yml`. Use `openapi-generator-maven-plugin` to generate interfaces and models.

**Trade-off:**
- **Pros:** 
  - Clear contract. 
  - Enables parallel frontend/backend work. 
  - Ability to expose api-docs via swagger-ui for ease of use.
- **Cons:** Adds a build step and some verbosity.

#### 3) Caching Strategy with Redis
**Decision:** Use Redis caching on GET endpoints with Spring’s `@Cacheable`.

**Trade-off:**
- **Pros:** 
  - Major read-performance boost and reduces DB and external service load. Also, declarative via annotations.
- **Cons:** 
  - Added infrastructure (Redis). Consistency concerns addressed via simple invalidation using `@CacheEvict(allEntries = true)` on writes-safe but potentially inefficient with frequent writes. Could evolve to more granular eviction.

#### 4) Handling Nested Collections in JPA
**Decision:** Store product tags (`List<String>`) as a single comma-separated `String` via a custom JPA `AttributeConverter`.

**Trade-off:**
- **Pros:** 
  - Avoids JPA limitation of nesting `@ElementCollection` inside another `@ElementCollection`. Also, keeps Java domain model clean.
- **Cons:** 
  - Prevents direct DB queries on individual tags. If needed, model tags can be a proper entity with a one-to-many relationship with the product entity.

#### 5) Database Schema Management
**Decision:** Use Hibernate automatic schema generation (`ddl-auto: update`) instead of Liquibase/Flyway.

**Trade-off:**
- **Pros:** 
  - Fast and simple for development/prototyping. 
  - No setup needed and schema syncs automatically with entities.
- **Cons:**
  - Not suitable for production as there is no version control, rollbacks are difficult, potential data loss on complex changes.
  - Liquibase/Flyway can be used for production-grade, versioned migrations.
