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
  - Rate limiting via Bucket4j with Caffeine JCache (configurable properties)
  - Retry with backoff using Spring Retry + AOP
  - Resilience4j integration (circuit breaker + retry)
  - Shedlock for distributed scheduled jobs
  - Spring Batch for batch job processing
- Data & Persistence
  - Spring Data JPA + MyBatis (hybrid usage)
  - Hibernate Envers for entity change audit trails
- Messaging & External Integrations
  - Email service (attachments supported)
  - SFTP file transfer via WinSCP scripts
  - Google Firebase Admin SDK
  - Google Pay decryption (Tink)
  - Alert notifications via Slack webhook and support email
- Observability
  - Actuator health endpoints (management port 8444)
  - Micrometer Prometheus metrics
  - Micrometer Tracing with Brave (distributed tracing)
  - Logback + Logstash encoder
  - Request logging & JSON masking utilities
- API & Docs
  - RFC7807 problem responses (consistent error format)
- Build Quality
  - OWASP Dependency-Check (optional via NVD API key)
  - Spring properties cleaner plugin
- Optional Libraries
  - ISO8583 message parsing (j8583)
  - Fake test data generation (JavaFaker)

## Customization Checklist (When Using as a Base for a New Project)

If you clone this template to start a new application, go through the following checklist to adapt it to your project.

### 1. Maven Project Identity (`pom.xml`)
- `<groupId>` → change from `com` to your organization (e.g., `com.mycompany`)
- `<artifactId>` → rename to your project name (e.g., `my-service`)
- `<name>` and `<description>` → update to describe your project
- `<url>` → update to your repository URL
- `<developer>` block → replace with your own details

### 2. Java Package Structure
- Rename the base package from `com` to match your groupId (e.g., `com.mycompany.myservice`)
- Update all import statements and component scan paths accordingly
- Update `Application.java` main class package declaration

### 3. Application Name & Context Path (all `application-*.yml`)
- `spring.application.name` → rename from `spring` to your service name
- `server.servlet.context-path` → change `/spring` to your desired URL prefix (e.g., `/my-service`)
- `management.server.base-path` → update to match your context path
- `rate.limit.endpoints` keys → update all endpoint paths to match your new context path

### 4. Database
- Update datasource URL, username, and password (or set via environment variables)
- Replace `src/main/resources/db_script/setup.sql` with your own schema
- Keep `spring-batch.sql` only if you use Spring Batch; remove it otherwise

### 5. Security & Keystores
- Generate new `keystore.p12` and `truststore.p12` under `src/main/resources/security/` using Keystore Explorer or `keytool`
- Update `key-alias` in all yml files to match your new keystore alias (currently `spring`)
- Replace API key files under `src/main/resources/security/api key/` with your own

### 6. Mail (SMTP)
- Update `spring.mail.*` settings or provide via environment variables: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_SENDER`
- Update `alert.support.email` addresses in all yml files to your team's emails

### 7. Google Services
- Replace `src/main/resources/security/google/firebase/firebase-service-account.json` with your Firebase project credentials
- Replace Google Pay keys under `src/main/resources/security/google/pay/` with your own merchant keys
- Remove `FirebaseConfig`, `FirebaseService`, `GooglePayConfig`, `GooglePayService` if not needed

### 8. SSHJ / SFTP (Optional)
- Update `src/main/resources/ftp_script/` WinSCP scripts with your SFTP server details, or remove if not used

### 9. Alert Notifications
- Set `alert.slack.webhook-url` in each yml file (or remove the Slack alert integration if not needed)
- Update `alert.support.email.to` and `alert.support.email.cc` to your team addresses

### 10. CORS
- Set `ALLOWED_ORIGINS` environment variable to your allowed frontend/client origins

### 11. Render Deployment (`render.yaml`)
- Rename service names (`spring-prod`, `spring-uat`) to match your project
- Update environment variable keys if you removed or renamed any integrations

### 12. CI/CD (`.github/workflows/build.yml`)
- Update GitHub Secrets to match your Render services and app-specific credentials
- Adjust branch names if your branching strategy differs

### 13. Logback (`logback-spring.xml`)
- Review log file paths and application name references

### 14. Remove Unused Template Code
- `src/main/java/com/api/template/` — sample REST controllers (JAX-RS + Spring MVC)
- `src/main/java/com/service/template/` — sample service, thread service, API caller
- `src/main/java/com/pojo/template/` — sample POJO
- `src/main/java/com/validation/template/` — sample custom validator
- `src/main/java/com/enums/` — sample enums (`CardType`, `ResponseCode`)
- `src/main/java/com/service/bank/` — sample card service

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
- ALLOWED_ORIGINS
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

Actuator runs on a dedicated management port (8444) with base path `/spring`:
- Health: GET /spring/actuator/health (port 8444)
- Metrics: GET /spring/actuator/prometheus (port 8444)

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
  - SPRING_DATASOURCE_*, SMTP_*, Firebase creds

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
