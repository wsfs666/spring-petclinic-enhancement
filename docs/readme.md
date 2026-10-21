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







## 10.Python API Probe

A lightweight Python CLI is included for repeatable runtime validation of PetClinic REST APIs. The tool sends sequential read-only HTTP requests, records request latency and HTTP status, classifies failures, and writes each result as a JSONL record.

### Structure

```text
scripts/
└── api_probe/
    ├── __init__.py
    ├── __main__.py
    ├── client.py
    ├── runner.py
    └── report.py

tests/
└── python/
    └── test_api_probe.py
```

### Setup

Create and activate a Python virtual environment from the repository root:

```bash
python3 -m venv .venv
source .venv/bin/activate
```

Install the HTTP client dependency:

```bash
python -m pip install requests
```

The `.venv/` directory should not be committed to Git.

### Usage

Start the PetClinic application, then run the probe from the repository root:

```bash
python -m scripts.api_probe \
  --base-url http://localhost:8080 \
  --count 10 \
  --timeout 2 \
  --output api-probe.jsonl
```

Arguments:

- `--base-url`: Base URL of the PetClinic service.
- `--count`: Number of sequential requests to execute.
- `--timeout`: Per-request timeout in seconds.
- `--output`: Path of the generated JSONL report.

The current probe targets a read-only PetClinic REST endpoint and intentionally executes requests sequentially rather than using asynchronous or concurrent request execution.

### Output

Each request produces one JSON object on a separate line:

```json
{"status": 200, "latency_ms": 4.32, "success": true, "error_type": null}
{"status": 404, "latency_ms": 3.91, "success": false, "error_type": "http_4xx"}
{"status": null, "latency_ms": 1.02, "success": false, "error_type": "connection_error"}
{"status": null, "latency_ms": 1001.14, "success": false, "error_type": "timeout"}
```

Failure classification:

- HTTP `2xx–3xx` → successful request
- HTTP `4xx` → `http_4xx`
- HTTP `5xx` → `http_5xx`
- Connection failure → `connection_error`
- Request timeout → `timeout`

HTTP errors and network failures are retained in the report rather than terminating the complete probe run.

### Tests

Python unit tests mock the underlying HTTP client so they do not require a running PetClinic application.

Run the tests from the repository root:

```bash
python -m unittest discover \
  -s tests/python \
  -p "test_api_probe.py"
```

Current unit-test coverage includes:

- Successful HTTP response
- HTTP 4xx response
- Connection failure
- Request timeout




