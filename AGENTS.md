# AGENTS.md

## Repo Shape
- No root Gradle build or wrapper; each `services/<name>` folder is a separate Gradle project with its own `gradlew`, `settings.gradle`, and `build.gradle`.
- Services are `config-server`, `discovery`, `gateway`, `customer`, `product`, `order`, `payment`, and `notification`.
- Runtime config lives in `services/config-server/src/main/resources/configurations/*.yml`; most service `application.yml` files only set `spring.application.name` and `optional:configserver:http://localhost:8888`.

## Commands
- Run Gradle commands from the service directory, e.g. use `workdir=services/order` with `./gradlew test`; root commands will fail because there is no root wrapper.
- Service tests: `./gradlew test`.
- Single test: `./gradlew test --tests com.mclavo.ecommerce.PaymentApplicationTests`; current order/product test classes use `com.mclavo.order.OrderApplicationTests` and `com.mclavo.product.ProductApplicationTests`.
- Focused build without tests: `./gradlew bootJar -x test`.
- Run one service: `./gradlew bootRun`.
- Start infrastructure from repo root: `docker compose up -d`.
- Full local startup order: infrastructure, `config-server`, `discovery`, domain services, then `gateway`.
- No CI, pre-commit, lint, formatter, or root task-runner config is present; do not invent mandatory checks.

## Runtime
- Java 21 toolchain, Gradle 8.14.4 wrappers, Spring Boot 4.0.6, and Spring Cloud 2025.1.1 are configured per service.
- Docker Compose exposes Postgres `5432`, MongoDB `27017`, Kafka `9092`, MailDev `1080`/`1025`, Zipkin `9411`, Keycloak `9098`, pgAdmin `5050`, and Mongo Express `8081`.
- Configured service ports: config `8888`, discovery `8761`, gateway `8222`, customer `8090`, product `8050`, order `8070`, payment `8060`, notification `8040`.
- Keycloak issuer is `http://localhost:9098/realms/ecommerce`; gateway and domain services require JWT except `/actuator/health`.
- Notification service denies all HTTP except `/actuator/health`; it is Kafka/email driven, not REST driven.
- VS Code launch configs read an ignored root `.env`; do not commit `.env`.

## Data
- Postgres init creates `product`, `orders`, and `payment` databases via `docker/postgres/init/01-create-databases.sql`.
- Product schema uses Flyway and `ddl-auto: validate`; product schema changes need migrations under `services/product/src/main/resources/db/migration`.
- Order and payment use JPA `ddl-auto: update`; customer and notification use MongoDB.

## Kafka
- Kafka topics are provisioned as infrastructure through Docker Compose (`kafka-init` + `create-topics.sh` + `topics.env`); broker auto-topic creation is disabled and `NewTopic` beans are intentionally avoided.
- Current saga topics are: `product.reservation.requested`, `product.reservation.succeeded`, `product.reservation.failed`, `payment.requested`, `payment.confirmed`, `payment.failed`, `order.confirmed`, `order.cancelled`, and `notification.requested`.
- Order Service produces `product.reservation.requested`, `payment.requested`, `order.confirmed`, `order.cancelled`, and `notification.requested`.
- Product Service produces `product.reservation.succeeded` and `product.reservation.failed`.
- Payment Service produces `payment.confirmed` and `payment.failed`.
- Event records are duplicated per service instead of using a shared contracts module; keep schemas, serialization settings, and Kafka JSON type mappings aligned across services.

## Flow
- The authoritative Saga documentation lives in `docs/order-processing-saga.md`; always follow that file before changing the order workflow.
- This Saga is event-driven but orchestrated by Order Service, not choreography-based.
- Order Service owns the order lifecycle and decides the next step based on Product and Payment events.
- Order Service uses OpenFeign only for synchronous pre-validation:
  - Validate customer through Customer Service
- Order Service must not call Product Service synchronously to enrich order lines or retrieve product data.
- After customer validation, Order Service creates the order with `PRODUCT_RESERVATION_PENDING` status, stores requested product IDs and quantities, then publishes `product.reservation.requested`.
- Product Service consumes `product.reservation.requested`, validates inventory, reserves stock, resolves product snapshot data, then publishes:
  - `product.reservation.succeeded`, or
  - `product.reservation.failed`
- `product.reservation.succeeded` must include the reserved product snapshots and confirmed prices; those prices are the source of truth for order total and payment amount.
- Order Service consumes `product.reservation.succeeded`, stores product snapshots in order lines, calculates `totalAmount`, updates order status to `PRODUCT_RESERVED`, then publishes `payment.requested`.
- Order Service consumes `product.reservation.failed`, updates order status to `PRODUCT_RESERVATION_FAILED`, then publishes `notification.requested`.
- Payment Service consumes only `payment.requested`, creates/processes the payment using the amount provided by Order Service, then publishes:
  - `payment.confirmed`, or
  - `payment.failed`
- Payment Service must not fetch products and must not recalculate prices.
- Order Service consumes `payment.confirmed`, updates order status to `CONFIRMED`, then publishes:
  - `order.confirmed`
  - `notification.requested`
- Order Service consumes `payment.failed`, updates order status to `PAYMENT_FAILED`, then publishes:
  - `order.cancelled`
  - `notification.requested`
- Product Service consumes final order events:
  - `order.confirmed` commits reserved stock
  - `order.cancelled` releases reserved stock
- Notification Service consumes only `notification.requested`, stores Mongo `Notification`, and sends email through MailDev.
- `order.cancelled` is an integration event for compensation, not necessarily an internal Order status.
- Do not reintroduce synchronous business chaining between Order, Product, and Payment.

## Code Notes
- Order Feign clients use service names and paths in annotations; `application.client.*` URLs in config are not currently wired into those clients.
- `FeignSecurityConfig` propagates the current JWT to downstream Feign calls; keep that behavior when refactoring order clients.
- Gateway routes only customers, orders, products, and payments under `/api/v1/**`; notification has no gateway route.
- `notification` intentionally depends on `spring-boot-starter-webmvc` because Eureka client registration needs it in this Boot 4 setup; do not remove it as unused.
- Customer uses MapStruct; generated mapper implementations live under `build` and should not be edited.