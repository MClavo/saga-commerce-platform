# Payment API

Base path: `/api/v1/payments`

Payment creation is saga-driven through Kafka after Product Service reserves stock. The public API only exposes payment state inspection and demo-only confirm/fail actions for frontend saga demonstrations.

Gateway access: `GET` endpoints require `ROLE_ORDER_MANAGER` or `ROLE_ADMIN`. Demo `POST /demo/orders/{orderId}/confirm` and `POST /demo/orders/{orderId}/fail` require `ROLE_ADMIN`. Other payment paths are denied by the gateway.

## Payment Status

```text
PENDING
CONFIRMED
FAILED
```

## Payment Response

```json
{
  "id": 1,
  "paymentReference": "PAY-ORD-42",
  "amount": 37.98,
  "paymentMethod": "CREDIT_CARD",
  "orderId": 42,
  "status": "PENDING",
  "createdAt": "2026-05-13T10:15:30",
  "updatedAt": null
}
```

## List Payments

```http
GET /api/v1/payments
```

Response: `200 OK`

```json
[
  {
    "id": 1,
    "paymentReference": "PAY-ORD-42",
    "amount": 37.98,
    "paymentMethod": "CREDIT_CARD",
    "orderId": 42,
    "status": "PENDING",
    "createdAt": "2026-05-13T10:15:30",
    "updatedAt": null
  }
]
```

## Get Payment

```http
GET /api/v1/payments/{id}
```

Response: `200 OK` with a payment response.

Errors: `404 Not Found` when the payment does not exist.

## Get Payment By Order

```http
GET /api/v1/payments/orders/{orderId}
```

Response: `200 OK` with the latest payment response for the order.

Errors: `404 Not Found` when no payment exists for the order.

## Confirm Demo Payment

```http
POST /api/v1/payments/demo/orders/{orderId}/confirm
```

Request body: none.

Response: `200 OK` with the payment response after status becomes `CONFIRMED`.

Side effect: publishes `payment.confirmed` for Order Service.

Errors: `404 Not Found` when no payment exists for the order, `409 Conflict` when the payment is already `FAILED`.

## Fail Demo Payment

```http
POST /api/v1/payments/demo/orders/{orderId}/fail
```

Request body: none.

Response: `200 OK` with the payment response after status becomes `FAILED`.

Side effect: publishes `payment.failed` for Order Service.

Errors: `404 Not Found` when no payment exists for the order, `409 Conflict` when the payment is already `CONFIRMED`.

## Frontend Flow

1. Create an order with `POST /api/v1/orders`.
2. Poll `GET /api/v1/orders/{id}` until status is `AWAITING_PAYMENT`.
3. Poll `GET /api/v1/payments/orders/{orderId}` until payment status is `PENDING`.
4. Call confirm or fail demo endpoint.
5. Continue polling the order until it reaches `CONFIRMED` or `PAYMENT_FAILED`.
