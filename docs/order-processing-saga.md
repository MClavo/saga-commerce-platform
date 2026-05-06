# Order Processing Saga

This project implements an **event-driven Saga pattern** to guarantee business consistency across distributed services without distributed transactions.

Each service owns its own database and performs **local transactions only**. Cross-service coordination is handled asynchronously through Apache Kafka.

## Communication Strategy

### OpenFeign (Synchronous)

Used only for **immediate validation or data retrieval** before an order is created.

**Order Service** performs:

- Customer validation (`Customer Service`)
- Product information retrieval (`Product Service`)

### Kafka (Asynchronous)

Used for:

- Distributed business workflows
- State transitions
- Compensation actions
- Notifications

Any operation that modifies another service's state must be event-driven.

---

# Saga Flow

## 1. Order Creation

**Order Service**

Actions:

- Validate customer
- Retrieve product information
- Create order with status `PENDING`

Produces:

- `order.created`

---

## 2. Stock Reservation

**Product Service**

Consumes:

- `order.created`

Actions:

- Validate inventory
- Reserve stock

Produces:

Success:

- `product.reservation.succeeded`

Failure:

- `product.reservation.failed`

---

## 3. Payment Processing

**Payment Service**

Consumes:

- `product.reservation.succeeded`

Actions:

- Create payment
- Process payment

Produces:

Success:

- `payment.confirmed`

Failure:

- `payment.failed`

---

## 4. Order Finalization

**Order Service**

Consumes:

- `product.reservation.failed`
- `payment.confirmed`
- `payment.failed`

Actions:

Success:

- Update order to `CONFIRMED`

Failure:

- Update order to `CANCELLED`

Produces:

Success:

- `order.confirmed`

Failure:

- `order.cancelled`

In both cases:

- `notification.requested`

---

## 5. Inventory Finalization

**Product Service**

Consumes:

- `order.confirmed`
- `order.cancelled`

Actions:

- Commit reserved stock on success
- Release reserved stock on cancellation

---

## 6. Notification

**Notification Service**

Consumes:

- `notification.requested`

Actions:

- Persist notification
- Send email/message

---

# Service Event Matrix

| Service | Consumes | Action | Produces |
|---|---|---|---|
| Order Service | HTTP Request | Validate customer, retrieve product data, create `PENDING` order | `order.created` |
| Product Service | `order.created` | Reserve stock | `product.reservation.succeeded` / `product.reservation.failed` |
| Payment Service | `product.reservation.succeeded` | Process payment | `payment.confirmed` / `payment.failed` |
| Order Service | `product.reservation.failed` | Cancel order | `order.cancelled`, `notification.requested` |
| Order Service | `payment.confirmed` | Confirm order | `order.confirmed`, `notification.requested` |
| Order Service | `payment.failed` | Cancel order | `order.cancelled`, `notification.requested` |
| Product Service | `order.confirmed` | Commit reserved stock | — |
| Product Service | `order.cancelled` | Release reserved stock | — |
| Notification Service | `notification.requested` | Persist and send notification | — |

---

# Failure Scenarios

## Stock Reservation Failure

```text
order.created
    ↓
product.reservation.failed
    ↓
order.cancelled
    ↓
notification.requested
```

Payment is never triggered.

## Payment Failure

```text
order.created
    ↓
product.reservation.succeeded
    ↓
payment.failed
    ↓
order.cancelled
    ↓
notification.requested
    ↓
stock released
```

---

# Design Principles

- No distributed transactions
- No synchronous business chaining
- Event-driven state transitions
- Explicit compensation actions
- Eventual consistency
- Idempotent consumers
- Domain events represent business facts, not technical commands