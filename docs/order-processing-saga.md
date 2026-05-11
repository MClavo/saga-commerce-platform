# Order Processing Saga

This project implements an **event-driven orchestrated Saga pattern** to guarantee business consistency across distributed services without using distributed transactions.

Each service owns its own database and executes **local transactions only**. Cross-service coordination is handled asynchronously through Kafka.

The **Order Service** acts as the Saga orchestrator. It owns the order lifecycle and decides the next step based on events produced by Product and Payment services.

---

# Communication Strategy

## Synchronous Communication — OpenFeign

Used only for immediate validation before an order is created.

**Order Service** performs:
- Customer validation (`Customer Service`)

Order Service does **not** call Product Service synchronously to enrich order lines.

Product snapshots such as:
- Product ID
- Product name
- Unit price

are returned by Product Service as part of the stock reservation result.

---

## Asynchronous Communication — Kafka

Used for:
- Distributed business workflows
- State transitions
- Compensation actions
- Notifications

Any operation that modifies another service's state must be event-driven.

---

# Saga Flow

## 1. Order Creation

### :contentReference[oaicite:1]{index=1}

### Actions
- Validate customer
- Create order with status `PRODUCT_RESERVATION_PENDING`
- Store requested product IDs and quantities
- Publish stock reservation request

### Produces
- `product.reservation.requested`

---

## 2. Stock Reservation

### :contentReference[oaicite:2]{index=2}

### Consumes
- `product.reservation.requested`

### Actions
- Validate inventory
- Reserve stock
- Resolve product snapshot data required by the order:
  - Product ID
  - Product name
  - Quantity
  - Unit price

### Produces

#### Success
- `product.reservation.succeeded`

#### Failure
- `product.reservation.failed`

The success event includes the reserved product snapshots and confirmed prices.

These prices become the **single source of truth** for:
- Order total
- Payment amount

---

## 3. Product Reservation Result

### :contentReference[oaicite:3]{index=3}

### Consumes
- `product.reservation.succeeded`
- `product.reservation.failed`

### On Success

#### Actions
- Store product snapshots in order lines
- Calculate `totalAmount`
- Update order status to `PRODUCT_RESERVED`
- Publish payment request

#### Produces
- `payment.requested`

### On Failure

#### Actions
- Update order status to `PRODUCT_RESERVATION_FAILED`
- Publish notification request

#### Produces
- `notification.requested`

---

## 4. Payment Processing

### :contentReference[oaicite:4]{index=4}

### Consumes
- `payment.requested`

### Actions
- Create payment
- Process payment using the amount provided by Order Service

### Produces

#### Success
- `payment.confirmed`

#### Failure
- `payment.failed`

Payment Service does **not** fetch products and does **not** recalculate prices.

---

## 5. Order Finalization

### :contentReference[oaicite:5]{index=5}

### Consumes
- `payment.confirmed`
- `payment.failed`

### On Payment Success

#### Actions
- Update order status to `CONFIRMED`
- Publish order confirmation
- Publish notification request

#### Produces
- `order.confirmed`
- `notification.requested`

### On Payment Failure

#### Actions
- Update order status to `PAYMENT_FAILED`
- Publish cancellation event
- Publish notification request

#### Produces
- `order.cancelled`
- `notification.requested`

---

## 6. Inventory Finalization

### :contentReference[oaicite:6]{index=6}

### Consumes
- `order.confirmed`
- `order.cancelled`

### Actions

#### On Confirmation
- Commit reserved stock

#### On Cancellation
- Release reserved stock

---

## 7. Notification

### :contentReference[oaicite:7]{index=7}

### Consumes
- `notification.requested`

### Actions
- Persist notification
- Send email/message

---

# Kafka Topics

## Produced by Order Service
- `product.reservation.requested`
- `payment.requested`
- `order.confirmed`
- `order.cancelled`
- `notification.requested`

## Produced by Product Service
- `product.reservation.succeeded`
- `product.reservation.failed`

## Produced by Payment Service
- `payment.confirmed`
- `payment.failed`

---

# Order Statuses

```java
enum OrderStatus {
    PRODUCT_RESERVATION_PENDING,
    PRODUCT_RESERVED,
    CONFIRMED,
    PRODUCT_RESERVATION_FAILED,
    PAYMENT_FAILED
}
```

---

# Service Event Matrix

| Service | Consumes | Action | Produces |
|---------|----------|--------|----------|
| Order Service | HTTP Request | Validate customer, create order | `product.reservation.requested` |
| Product Service | `product.reservation.requested` | Reserve stock | `product.reservation.succeeded`, `product.reservation.failed` |
| Order Service | `product.reservation.succeeded` | Store snapshots, calculate total, set `PRODUCT_RESERVED` | `payment.requested` |
| Order Service | `product.reservation.failed` | Set `PRODUCT_RESERVATION_FAILED` | `notification.requested` |
| Payment Service | `payment.requested` | Process payment | `payment.confirmed`, `payment.failed` |
| Order Service | `payment.confirmed` | Confirm order | `order.confirmed`, `notification.requested` |
| Order Service | `payment.failed` | Set `PAYMENT_FAILED` | `order.cancelled`, `notification.requested` |
| Product Service | `order.confirmed` | Commit stock | — |
| Product Service | `order.cancelled` | Release stock | — |
| Notification Service | `notification.requested` | Persist and send notification | — |

---

# Failure Scenarios

## Stock Reservation Failure

```text
product.reservation.requested
    ↓
product.reservation.failed
    ↓
notification.requested
```

Payment is never triggered.

---

## Payment Failure

```text
product.reservation.requested
    ↓
product.reservation.succeeded
    ↓
payment.requested
    ↓
payment.failed
    ↓
order.cancelled
    ↓
notification.requested
    ↓
stock released by Product Service
```

---

# Design Principles

- No distributed transactions
- Order Service orchestrates the Saga
- No synchronous product enrichment from Order Service
- No N+1 remote product lookups
- Product Service owns stock validation and product price snapshots
- Order Service owns order lifecycle and total calculation
- Payment Service processes only the provided amount
- Event-driven state transitions
- Explicit compensation through `order.cancelled`
- Eventual consistency
- Idempotent consumers
- Domain events represent meaningful business transitions

---

# Design Decision

`order.cancelled` exists as an **integration event**, not necessarily as an internal order status.

Internal state remains explicit:
- `PRODUCT_RESERVATION_FAILED`
- `PAYMENT_FAILED`

This preserves failure semantics inside the Order aggregate while still exposing a clear compensation event to external services.