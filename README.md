# ShopEase Checkout

An e-commerce checkout system built to demonstrate how **GoF design patterns** enable extensible, change-resilient software architecture.
## Objective

> Transition from a fixed-logic approach to a dynamic, extensible-logic approach. Design and implement software solutions that can accommodate 10–20% change in requirements without requiring a full refactor.

This project consolidates three mini-projects (Payment Gateway, Notification Engine, Shipping Calculator) into a single cohesive checkout system. Each subsystem uses design patterns that decouple *what* the system does from *how* it does it — so adding a new payment provider, notification channel, or shipping rule requires **one new class and zero changes to existing code**.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 25, Spring Boot 4, Hibernate 7, Spring Security + JWT |
| Database | PostgreSQL 18 (native enums, UUIDv7), Flyway migrations |
| Email | AWS SES (HTML templates) |
| SMS | Hubtel API |
| Frontend | React 19, Vite 7, TypeScript, React Router v7, Tailwind CSS v4 |
| API Docs | OpenAPI 3 / Swagger UI |
| Infrastructure | Docker Compose |

## Getting Started

### 1. Configure credentials

```bash
cp .env.example .env
# Edit .env with your AWS SES and Hubtel SMS credentials
```

### 2. Start with Docker

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

### 3. Or run services individually (development)

```bash
# Terminal 1: Start PostgreSQL (exposed on host port 5433)
docker compose up postgres

# Terminal 2: Start backend
cd backend
# Export required env variables (Docker Compose reads .env automatically, but Maven does not)
export $(grep -v '^#' ../.env | xargs)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/shopease
./mvnw spring-boot:run

# Terminal 3: Start frontend
cd frontend && npm install && npm run dev
```

### 4. Run tests

```bash
cd backend
./mvnw test                        # Run all tests
./mvnw jacoco:report               # Coverage report → target/site/jacoco/index.html
```

---

## Design Patterns in Action

### Strategy Pattern

**Problem:** The checkout system needs to support multiple payment providers, shipping methods, and notification channels. Hardcoding each option creates rigid `if/else` chains that must be modified every time a new option is added.

**Solution:** Define a common interface for each concern. Each implementation is a self-contained strategy that can be swapped at runtime.

| Interface | Implementations | Selected By |
|-----------|----------------|-------------|
| `PaymentProcessor` | `StripePaymentAdapter`, `PayPalPaymentAdapter`, `CryptoPaymentAdapter` | User's payment choice at checkout |
| `ShippingStrategy` | `StandardShipping`, `ExpressShipping` | User's shipping choice in cart |
| `NotificationSender` | `AwsSesEmailSender`, `HubtelSmsSender` | User's notification preference at checkout |

**Extensibility:** Adding M-Pesa as a payment option requires creating one class (`MpesaPaymentAdapter implements PaymentProcessor`) and annotating it with `@Component`. The factory auto-discovers it, the checkout flow uses it — zero changes to `OrderServiceImpl`, `PaymentProcessorFactory`, or any existing adapter.

### Factory Pattern

**Problem:** The caller (checkout service) shouldn't know which concrete implementation to instantiate. It only knows a runtime key like `"STRIPE"` or `"EMAIL"`.

**Solution:** Factory classes use Spring's dependency injection to auto-discover all implementations and index them by key.

| Factory | Creates | Discovery |
|---------|---------|-----------|
| `PaymentProcessorFactory` | `PaymentProcessor` instances | Collects all `PaymentProcessor` beans, indexes by `getKey()` |
| `NotificationSenderFactory` | `NotificationSender` instances | Collects all `NotificationSender` beans, indexes by `getChannel()` |

```java
// PaymentProcessorFactory — Spring auto-discovers all PaymentProcessor beans
public PaymentProcessorFactory(List<PaymentProcessor> processors) {
    this.processorMap = processors.stream()
            .collect(Collectors.toMap(PaymentProcessor::getKey, Function.identity()));
}

public PaymentProcessor create(String key) {
    return Optional.ofNullable(processorMap.get(key.toUpperCase()))
            .orElseThrow(() -> new IllegalArgumentException("Unknown payment method: " + key));
}
```

### Adapter Pattern

**Problem:** Each external payment API (Stripe, PayPal, Crypto) has a different request/response shape. The checkout service shouldn't know these details.

**Solution:** Each adapter wraps an external API and normalizes it to the common `PaymentProcessor` interface, returning a standardized `PaymentResult`.

```
Checkout Service → PaymentProcessor interface → StripePaymentAdapter → Stripe API
                                               → PayPalPaymentAdapter → PayPal API
                                               → CryptoPaymentAdapter → Crypto API
```

The checkout service calls `processor.processPayment(request)` and receives a `PaymentResult` — it never knows which provider handled it.

### Observer Pattern

**Problem:** After an order is placed, the system needs to send notifications. Coupling notification logic directly into the checkout flow means the checkout service must import and coordinate notification code — violating the Single Responsibility Principle.

**Solution:** The checkout service publishes a domain event (`OrderPlacedEvent`). A separate listener (`NotificationEventListener`) reacts to it asynchronously. Neither knows about the other.

```
OrderServiceImpl                          NotificationEventListener
     │                                           │
     ├─ save order                                │
     ├─ publish OrderPlacedEvent ──────────►  @TransactionalEventListener
     └─ return response immediately               ├─ send EMAIL (async)
                                                  └─ send SMS (async)
```

Key detail: `@TransactionalEventListener` ensures the event fires *after* the database transaction commits — preventing a race condition where the listener tries to look up an order that hasn't been committed yet.

### Decorator Pattern

**Problem:** Notification sending can fail (network issues, provider outages). We need retry logic and fallback behavior, but we don't want to pollute each sender implementation with retry code.

**Solution:** `RetryableNotificationSender` wraps any `NotificationSender` transparently. It retries up to N times (configurable via `application.yml`), and if all retries fail for a non-email channel, falls back to email.

```
Checkout → RetryableNotificationSender → HubtelSmsSender (attempt 1, 2, 3)
                                        → AwsSesEmailSender (fallback)
```

The `HubtelSmsSender` doesn't know it's being retried. The retry count is externalized to `application.yml`, not hardcoded.

### Chain of Responsibility

**Problem:** Shipping discounts depend on multiple independent rules (order threshold, membership tier). These rules may change, be added, or be reordered.

**Solution:** `ShippingDiscountChain` collects all `ShippingDiscountRule` beans (via Spring DI), sorts them by priority, and applies them in sequence. Each rule decides independently whether it applies.

| Rule | Priority | Condition | Effect |
|------|----------|-----------|--------|
| `FreeShippingOverThresholdRule` | 10 | Order subtotal > $50 | Shipping = $0 |
| `GoldMemberDiscountRule` | 50 | User tier = GOLD | 20% off shipping |

**How priority works:** The priority value determines execution order — lower numbers run first. Rules are sorted ascending by `priority()` and applied in sequence. If a rule reduces the cost to zero, the chain breaks early (remaining rules are skipped). This is why `FreeShippingOverThresholdRule` (10) runs before `GoldMemberDiscountRule` (50) — if the order qualifies for free shipping, the Gold discount never runs because there's nothing left to discount.

The values (10, 30, 50) are spaced with gaps rather than sequential (1, 2, 3) so that new rules can be inserted between existing ones without renumbering. For example, a "Holiday 50% off" rule at priority 30 slots between free-shipping (10) and Gold discount (50) — no existing rule changes.

**Extensibility:** Adding a "Holiday 50% off shipping" promotion means creating one `@Component` class with `priority(30)`. No changes to the chain, existing rules, or the shipping service.

---

## Application Flow

```
┌─────────────┐     ┌──────────────┐     ┌───────────────────┐     ┌──────────────────┐
│  Register    │────►│  Browse &    │────►│     Checkout       │────►│  Confirmation    │
│  (or Login)  │     │  Build Cart  │     │                    │     │                  │
│              │     │              │     │  Payment Method    │     │  Order Details   │
│  Welcome     │     │  Shipping    │     │  Currency          │     │  Notifications   │
│  Email ✉️    │     │  Selection   │     │  Notification Pref │     │  Status          │
└─────────────┘     └──────────────┘     └───────────────────┘     └──────────────────┘
                                                   │
                                                   ▼
                           ┌──────────────────────────────────────┐
                           │        OrderServiceImpl              │
                           │                                      │
                           │  1. Calculate shipping (Strategy)    │
                           │  2. Apply discounts (Chain)          │
                           │  3. Convert currency                 │
                           │  4. Process payment (Factory+Adapter)│
                           │  5. Save order                       │
                           │  6. Publish event (Observer)         │
                           └──────────────────────────────────────┘
                                                   │
                                                   ▼
                           ┌──────────────────────────────────────┐
                           │    NotificationEventListener         │
                           │    (async, after commit)             │
                           │                                      │
                           │  For each selected channel:          │
                           │    RetryableNotificationSender       │
                           │      → Send (up to 3 retries)       │
                           │      → Fallback to email if needed   │
                           │      → Persist result to DB          │
                           └──────────────────────────────────────┘
```

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/register` | Public | Register new user (triggers welcome email) |
| `POST` | `/api/auth/login` | Public | Login, returns JWT |
| `GET` | `/api/auth/me` | JWT | Current user profile |
| `GET` | `/api/products` | Public | List all active products |
| `GET` | `/api/payment/methods` | Public | Available payment methods |
| `GET` | `/api/payment/currencies` | Public | Supported currencies + rates |
| `POST` | `/api/shipping/calculate` | JWT | Calculate shipping for cart |
| `POST` | `/api/orders/checkout` | JWT | Full checkout flow |
| `GET` | `/api/orders/{orderNumber}` | JWT | Order details + notification logs |
| `GET` | `/api/orders` | JWT | Current user's order history |

---

## "What-If" Scenarios — Proving Extensibility

The PDP objective requires the architecture to handle 10–20% requirement changes without a full refactor. Here are concrete scenarios and what each change would require:

### "Add M-Pesa as a payment option"
- **Create:** `MpesaPaymentAdapter implements PaymentProcessor` (one class)
- **Change:** Nothing. The factory auto-discovers it via Spring DI.
- **Pattern:** Strategy + Factory + Adapter

### "Add WhatsApp notifications"
- **Create:** `WhatsAppSender implements NotificationSender` (one class)
- **Add:** `WHATSAPP` to `NotificationChannel` enum
- **Change:** Nothing else. The factory discovers it, retry/fallback wraps it automatically.
- **Pattern:** Strategy + Factory + Decorator

### "Offer a holiday promotion: 50% off shipping for all users"
- **Create:** `HolidayShippingDiscountRule implements ShippingDiscountRule` with `priority(30)` (one class)
- **Change:** Nothing. The chain auto-discovers and applies it in priority order.
- **Pattern:** Chain of Responsibility

### "Add a PLATINUM tier with free shipping"
- **Add:** `PLATINUM` to `MembershipTier` enum
- **Create:** `PlatinumFreeShippingRule implements ShippingDiscountRule` (one class)
- **Change:** Nothing else. The discount chain picks it up automatically.
- **Pattern:** Chain of Responsibility

### "Switch from AWS SES to SendGrid for emails"
- **Create:** `SendGridEmailSender implements NotificationSender` (one class)
- **Remove:** `AwsSesEmailSender`
- **Change:** Nothing in the checkout flow, notification service, or retry logic.
- **Pattern:** Strategy + Factory

---

## Configuration & Externalization

Business rules are externalized to `application.yml` so they can change without code modifications:

```yaml
shipping:
  free-threshold: 50.0           # Orders above this amount get free shipping
  gold-discount-percent: 20      # Gold member discount percentage

currency:
  rates:
    USD: 1.0
    GHS: 15.5
    EUR: 0.92

notification:
  max-retries: 3                 # Retry attempts before fallback
```

In a production system, currency rates would come from the payment gateway provider. Business rules like `free-threshold`, `gold-discount-percent`, and `max-retries` could be managed via an admin panel backed by a database settings table — allowing changes at runtime without redeployment. Externalizing to config is the first step — it decouples the values from the code without overengineering.

---

## Database Design

PostgreSQL 18 with Flyway-managed migrations. Key design decisions:

- **UUIDv7 primary keys** — Time-ordered UUIDs (PostgreSQL 18 native `uuidv7()`) avoid B-tree index fragmentation, are globally unique, and don't leak information like auto-increment IDs.
- **Named enums** — `membership_tier` and `notification_channel` are PostgreSQL `CREATE TYPE ... AS ENUM`, mapped to Java enums via Hibernate's `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` for type safety at both layers.
- **Separate order number** — Internal PK is a UUIDv7 (`id`), public-facing identifier is `order_number` (format: `ORD-YYYYMMDD-XXXX`). Customers never see internal IDs.
- **Snapshotted order items** — `order_items` copies `product_name` and `price` at order time. If a product is later deleted or repriced, order history remains accurate.

---

## Project Structure

```
shopease-checkout/
├── backend/
│   └── src/main/java/com/shopease/checkout/
│       ├── common/           # Shared enums, config (UUIDv7 generator)
│       ├── dto/              # Request/response records
│       ├── entity/           # JPA entities (Lombok)
│       ├── mapper/           # Entity ↔ DTO converters
│       ├── repository/       # Spring Data JPA repositories
│       ├── security/         # JWT, auth, Spring Security config
│       ├── payment/          # Strategy + Factory + Adapter
│       ├── shipping/         # Strategy + Chain of Responsibility
│       ├── notification/     # Strategy + Factory + Decorator + Observer
│       └── order/            # Checkout orchestration + events
├── frontend/src/
│   ├── components/           # UI components (shadcn-style)
│   ├── lib/                  # API client, auth context, types
│   └── pages/                # Login, Register, Cart, Checkout, Confirmation
├── docker-compose.yml
└── .env.example
```

---

## Production Improvements (Out of Scope)

The following improvements are intentionally omitted to keep the project focused on design patterns. They represent production-grade practices I'm aware of and would implement in a real-world system:

### Security
- **Refresh tokens:** Opaque, server-stored, rotated on use. Access tokens in memory (15 min expiry), refresh tokens in HTTP-only secure cookies.
- **Token revocation / blacklisting** on logout
- **Rate limiting** on auth endpoints (e.g., Spring Cloud Gateway or Bucket4j)
- **Account lockout** after N failed login attempts
- **Password complexity requirements** and bcrypt/argon2 cost tuning
- **CSRF protection** if switching to cookie-based auth
- **Input sanitization** beyond Jakarta Validation (XSS, SQL injection at the boundary layer)

### Architecture & Infrastructure
- **Testcontainers** for integration tests against real PostgreSQL (current tests use H2 with `ddl-auto: create-drop`)
- **API versioning** (URL-based or header-based) for backwards compatibility
- **Pagination** on list endpoints (`/api/products`, `/api/orders`) using Spring Data's `Pageable`
- **Idempotency keys** on checkout to prevent duplicate orders from network retries
- **Caching** (Spring Cache + Redis) for product catalog and payment method listings to reduce database calls; event-driven invalidation on product updates
- **Circuit breaker** (Resilience4j) around external service calls (SES, Hubtel) to fail fast during outages
- **Distributed tracing** (Micrometer + Zipkin/Jaeger) for end-to-end request observability
- **Structured logging** (JSON format) for log aggregation (ELK, CloudWatch)
- **Health checks** (`/actuator/health`) with custom indicators for SES connectivity, DB pool status

### Notifications
- **Email verification** on registration (OTP or verification link)
- **Template engine** (Thymeleaf or FreeMarker) for email templates instead of inline HTML
- **Dead letter queue** for persistently failed notifications (SQS, RabbitMQ)
- **Webhook delivery** with exponential backoff for real-time integrations
- **Unsubscribe management** per notification type

### Database & Data
- **Read replicas** for query-heavy endpoints (order history, product catalog)
- **Database connection pooling** tuning (HikariCP `maximumPoolSize`, `connectionTimeout`)
- **Soft deletes** (`deleted_at` timestamp) instead of hard deletes for audit compliance
- **Event sourcing** for order lifecycle (CONFIRMED → SHIPPED → DELIVERED) with full audit trail
- **Flyway migration checksums** in CI to prevent accidental schema drift

### DevOps
- **CI/CD pipeline** (GitHub Actions) — build, test, lint, coverage gate, Docker push
- **Multi-stage Docker builds** with distroless or Alpine JRE base images
- **Kubernetes manifests** or Helm charts for container orchestration
- **Secrets management** (AWS Secrets Manager, HashiCorp Vault) instead of `.env` files
- **Blue-green or canary deployments** for zero-downtime releases

---

## Environment Variables

See `.env.example` for the full list. Required for notifications:

| Variable | Purpose |
|----------|---------|
| `AWS_ACCESS_KEY_ID` | AWS IAM credentials for SES |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM credentials for SES |
| `AWS_SES_REGION` | SES region (e.g., `eu-west-1`) |
| `AWS_SES_FROM_EMAIL` | Verified sender email in SES |
| `HUBTEL_CLIENT_ID` | Hubtel SMS API credentials |
| `HUBTEL_CLIENT_SECRET` | Hubtel SMS API credentials |
| `JWT_SECRET` | JWT signing key (min 256 bits) |
| `DB_PASSWORD` | PostgreSQL password |
