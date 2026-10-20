## Local Development

Prerequisites:


How to run:
./mvnw spring-boot:run

How to test:
baseline: ./mvnw clean verify
further: ./mvnw test

Database:
baseline: H2


Current Project Status:
basic functionality works normal. Current functionality includes:
- Vet page without access to CRUD
- Find/Add/edit Owner
- show/Add/Edit Pet
- show/Add visit
- Home page
- Error Page
  
  The application currently uses H2 as the default database.


## Run with PostgreSQL

### 1. Start PostgreSQL

docker compose up -d postgres

### 2. Verify the Container is healthy

docker compose ps

### 3. Start PetClinic with postgreSQL profile

./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres

### 4. Verify the Application

curl http://localhost:8080/api/owners/1

### 5. Stop the Environment

docker compose down

*The default development/test configuration uses H2.
*The `postgres` profile uses PostgreSQL with Flyway-managed schema migrations.






## 6. CI
Continuous Integration (CI)

This project uses GitHub Actions to automate build verification and regression testing.
The CI workflow runs on pushes and pull requests and performs the following steps:

Checks out the repository.
Configures the Java development environment.
Runs Maven build verification using ./mvnw clean verify.
Executes the automated test suite, including PostgreSQL integration tests powered by Testcontainers.
Reports build and test failures through GitHub Actions.






## 7.Testing
The project uses JUnit, Spring Boot Test, Testcontainers, PostgreSQL, and Flyway to verify application behavior across different testing scopes.

# Controller / Web-Layer Tests
Controller tests validate HTTP request handling, request validation, response status codes, and API behavior using MockMvc with mocked dependencies where appropriate.

# Integration Tests
Integration tests validate interactions between application components using real Spring-managed services, repositories, and databases rather than mocked persistence layers.

Coverage includes:
- **Service and transaction behavior:** Validates business operations, database persistence, and transactional consistency.
- **Pet creation regression:** Verifies database-generated Pet IDs, successful persistence, and Owner–Pet relationships.
- **Visit persistence:** Verifies Visit creation and its relationship with the associated Pet.

Integration tests use H2 where applicable and PostgreSQL through Testcontainers for database-specific verification.

# Database Integration Environment
PostgreSQL integration tests use Testcontainers to provision an isolated database. Flyway V1 and V2 migrations initialize the schema and sample data before tests execute.

The tests run locally and as part of the GitHub Actions CI pipeline through Maven verification.





## 8.Observability
add actuator dependency
Health Monitoring：
get http://localhost:8080/actuator/health

Runtime Metrics：
get http://localhost:8080/actuator/metrics/jdbc.connections.active
get http://localhost:8080/actuator/metrics/jdbc.connections.active


## 9.Performance Benchmarking

The project includes a lightweight HTTP performance baseline using hey.
The benchmark measures request throughput, latency percentiles, and HTTP response status distribution under a defined workload.





