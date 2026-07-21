# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

This is a **take-home test assignment** (тестовое задание "ora2java"), not a production service. The
author is a senior Kotlin QA automation engineer transitioning to SDE, targeting a **mid-level**
backend role — so changes should demonstrate solid, idiomatic backend craft and the reviewer will judge
correctness, structure, and attention to the spec. Prefer clarity and conventional Spring/JDBC patterns
over cleverness.

### Original requirements (from the task)

The service must:
1. Receive payment messages over **Kafka/RabbitMQ**. Payment format: `sum`, `type` (vacation / salary /
   bonus), `date`.
2. Expose a **REST** function to edit the `sum` of a specific payment.
3. Expose a **REST** function to fetch all payment fields (including id) by a specific `type` and `date`.
   This fetch must be **cached with a 5-minute TTL**.
4. **Once per hour**, compute balances per payment-type-per-date and store them in a separate table.
5. Allow querying the balance for a specific date and type.

### Code review

A senior-level review of the current code lives in **`docs/code-review.md`** — bugs (broken
`@NotBlank` validation, non-transactional balance rebuild), high-load/microservice concerns (no
ShedLock on the scheduled job, no DLQ/idempotency on the Rabbit consumer, missing indexes), and a
prioritized work list. Read it before reworking existing functionality, and keep it updated as
items are fixed.

### Known gaps vs. the spec (current code)

These are deviations from the requirements above — likely the highest-value things to address:
- **No caching** on `getPaymentsByTypeAndDate` — the spec requires a 5-minute TTL cache (e.g. Spring
  `@Cacheable` + a cache manager). Not yet implemented.
- **Balance job runs every 30s**, not hourly — `job.count-balances-by-type-and-date.cron` is
  `*/30 * * * * *`; the spec asks for once per hour.

## Commands

Build / test / run use the Gradle wrapper (Java 21 toolchain, Kotlin DSL build script):

```bash
./gradlew build          # compile + run all tests
./gradlew test           # run all tests
./gradlew bootRun        # run the app locally
./gradlew test --tests "com.road.to.payments.PaymentsApplicationTests"   # single test class
./gradlew test --tests "*.PaymentsApplicationTests.contextLoads"         # single test method
```

`bootRun` auto-starts the `docker-compose.yml` services (Postgres + RabbitMQ) via
`spring-boot-docker-compose` (`spring.docker.compose.enabled: true`). Postgres is exposed on
host port **5433** (mapped to container 5432); the datasource and RabbitMQ creds in
`application.yml` must match the compose file. On every startup `schema.sql` runs
(`spring.sql.init.mode: always`) — it **drops and recreates the `payments` schema** and reseeds
sample rows, so the DB is reset each run.

OpenAPI/Swagger UI is served by springdoc at `/swagger-ui.html` when running.

## Architecture

Spring Boot 3.5.6 payments service. Runs on **Jetty** — the Tomcat starter is explicitly excluded
in `build.gradle.kts`. Persistence is **plain JDBC** (`NamedParameterJdbcTemplate`), not JPA/Hibernate;
all SQL is hand-written inside the `*RepositoryImpl` classes.

Two data flows feed the system, and a scheduled job derives a summary table from them:

- **Ingest (write):** `PaymentDto` messages arrive on the durable RabbitMQ queue `paymentQueue`
  (declared in `RabbitConfig`, JSON-converted). `PaymentConsumer` (`@RabbitListener`) hands each to
  `PaymentsService.savePayment`, which inserts into `payments.payment`.
- **Query/update (REST):** `PaymentsController` (`/payments/*`) exposes `getPaymentsByTypeAndDate`,
  `getBalanceByTypeAndDate`, and `updatePaymentSum`, all delegating to `PaymentsService`.
- **Balance aggregation (scheduled):** `CountBalancesByTypeAndDateTask` runs on the cron in
  `application.yml` (`job.count-balances-by-type-and-date.cron`, default every 30s; `@EnableScheduling`
  is on `PaymentsApplication`). It recomputes `payments.balance` by **deleting all rows and
  re-inserting** `SUM(sum) GROUP BY date, type` from `payments.payment`. So `balance` is a derived
  cache, eventually consistent and rebuilt wholesale each tick — never written to directly.

### Layering and conventions

- `controller` → `service` (`PaymentsService` interface + `impl`) → `db/repository` (interface + `impl`).
  Repository interfaces live in `db/repository`, JDBC implementations in `db/repository/impl`.
- **MapStruct** mappers (`PaymentMapper`, `BalanceMapper`) convert between `db/entity` (`PaymentEntity`,
  `BalanceEntity`) and `model` DTOs. Note `BalanceMapper.balanceEntityFromPaymentEntity` bridges the
  aggregation step — `paymentsGroupedByTypeAndDate()` returns `PaymentEntity` rows holding the grouped
  sum, which are mapped to `BalanceEntity` for insertion.
- **Lombok** for constructors/loggers (`@RequiredArgsConstructor`, `@AllArgsConstructor`, `@Slf4j`).
  Lombok + MapStruct annotation processors are wired in `build.gradle.kts`; the
  `lombok-mapstruct-binding` processor is required for them to cooperate — keep it.
- `PaymentType` is an enum bound directly as a controller `@RequestParam` and persisted as its
  `VARCHAR` name.
- Validation errors (`@Valid` on request bodies) are turned into a `field -> message` map with HTTP 400
  by `GlobalExceptionHandler` (`@ControllerAdvice`).
- Log messages and Swagger descriptions are written in **Russian** — match that when editing.

### In-flight / rough edges

- `src/main/resources/query/FIND_PAYMENT_BY_TYPE_AND_DATE.sql` exists but is unused; `PaymentRepositoryImpl`
  has a `TODO` to move inline SQL into an external "QueryHolder". SQL currently lives as string literals
  in the repo impls.
- A Kafka starter is on the classpath (`spring-kafka`) but no producer/consumer is implemented; the
  Kafka/Zookeeper services in `docker-compose.yml` are commented out.

## Notes

- The project does not use a separate lint step; `./gradlew build` is the gate (compile + tests).
- `spring-boot-actuator` is included — management endpoints are available.
