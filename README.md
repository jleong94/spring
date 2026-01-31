# Spring Boot Base Project Template

A production-ready starter for building Spring Boot services with Java 21, MySQL, robust security, observability, and CI/CD.

## Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.9+
- MySQL 8.x
- Git
- Docker (optional, for containerization)
- Keystore Explorer (optional, for SSL/MTLS keystores)

## Key Features Implemented

- Security
  - Spring Security configuration with custom filters and authentication tokens
  - MTLS client-certificate detection utilities
  - Keystore/truststore support (resources/security/*.p12)
- Reliability
  - Rate limiting via Bucket4j (configurable properties)
  - Retry with backoff using Spring Retry + AOP
  - Resilience4j integration
  - Shedlock for distributed scheduled jobs
- Data & Persistence
  - Spring Data JPA + MyBatis (hybrid usage)
  - Hibernate Envers for entity change audit trails
- Messaging & External Integrations
  - Email service (attachments supported)
  - SSHJ for SSH/SFTP operations
  - Google Firebase Admin SDK
  - Google Pay decryption (Tink)
- Observability
  - Actuator health endpoints
  - Micrometer Prometheus metrics
  - Logback + Logstash encoder
  - Request logging & JSON masking utilities
- API & Docs
  - OpenAPI/Swagger UI (springdoc)
  - RFC7807 problem responses (consistent error format)
- Build Quality
  - OWASP Dependency-Check (optional via NVD API key)
  - Spring properties cleaner plugin

## Configuration Profiles

Available profiles:
- dev → src/main/resources/application-dev.yml
- test → src/main/resources/application-test.yml
- uat → src/main/resources/application-uat.yml
- prod → src/main/resources/application-prod.yml

To select a profile, set SPRING_PROFILES_ACTIVE.

### Environment Variables

Common variables (example naming):
- SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
- SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_SENDER
- KEYCLOAK_AUTH_SERVER_URL, KEYCLOAK_CLIENT_SECRET
- Firebase credentials (mounted file or env)

## Database Setup

Run the provided SQL scripts:
- src/main/resources/db_script/setup.sql
- src/main/resources/db_script/spring-batch.sql

## Local Setup

### Build JAR

```bash
# bash
mvn --batch-mode clean verify
```

JAR outputs to target/.

### Run (Dev)

```bash
# bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

Or:

```bash
# bash
java -Xms256m -Xmx2048m -jar target/spring-*.jar
```

### Windows Service (Optional)

```powershell
# powershell
java -Xms256m -Xmx512m -jar spring.jar
```

Use systemd on Linux as described in original guide if needed.

## Testing

- Unit tests: mvn test
- Integration tests: mvn verify (failsafe runs *ITests.java)
- Optional OWASP scan:
```bash
# bash
mvn --batch-mode clean verify -Dnvd.api.key=${NVD_API_KEY}
```

## Containerization

Dockerfile is multi-stage and uses Maven + JDK 21 to build, then JRE 21 to run.

```bash
# bash
docker build -t spring:latest .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod spring:latest
```

## Observability

- Health: GET /actuator/health
- Metrics: GET /actuator/prometheus
- API Docs: /swagger-ui.html (springdoc)

## CI/CD

GitHub Actions workflow (.github/workflows/build.yml):
- Builds and versions artifacts on pushes to:
  - uat, staging → UAT pipeline
  - main, master → Production pipeline
- Creates release tags and publishes JAR assets

Render deployment (Blueprint render.yaml + Actions):
- UAT service (branch: uat), Production service (branch: master)
- Required GitHub secrets:
  - RENDER_API_KEY
  - RENDER_SERVICE_ID_UAT
  - RENDER_SERVICE_ID_PROD
- App-specific secrets (per environment):
  - SPRING_DATASOURCE_*, SMTP_*, KEYCLOAK_*, Firebase creds

## Security Notes

- Keystores/truststores in resources/security/ for SSL/MTLS
- Ensure secrets are managed via environment variables or secret managers
- Block force pushes and enforce branch protections on main/master

## Project Roadmap

- [ ] Fluent Bit + OpenSearch log shipping and analysis

## Contributing

- Fork, branch, commit, push, PR

## License

Apache-2.0. See LICENSE.