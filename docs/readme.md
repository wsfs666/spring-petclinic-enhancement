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









