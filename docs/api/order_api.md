# Order API

Base path: `/api/v1/orders`

Order creation starts the order-processing saga. Product reservation, payment, stock compensation, and notifications continue asynchronously through Kafka. Payment confirmation/failure is handled by Payment Service demo endpoints, not by Order Service.

Gateway access: `POST` endpoints require any authenticated JWT. `GET` endpoints require `ROLE_ORDER_MANAGER` or `ROLE_ADMIN`.

## Order Status

```text
PRODUCT_RESERVATION_PENDING
AWAITING_PAYMENT
CONFIRMED
PRODUCT_RESERVATION_FAILED
PAYMENT_FAILED
```

## Payment Method

```text
CREDIT_CARD
DEBIT_CARD
PAYPAL
VISA
MASTERCARD
```

## Order Response

```json
{
  "id": 42,
  "reference": "ORD-42",
  "amount": 37.98,
  "paymentMethod": "CREDIT_CARD",
  "customerId": "customer-1",
  "status": "AWAITING_PAYMENT"
}
```

## Order Line Response

```json
{
  "id": 1,
  "productId": 5,
  "productName": "Claw Hammer",
  "quantity": 2,
  "unitPrice": 18.99
}
```

Before product reservation succeeds, `productName` and `unitPrice` may be `null`.

## List Orders

```http
GET /api/v1/orders
```

Response: `200 OK`

```json
[
  {
    "id": 42,
    "reference": "ORD-42",
    "amount": 37.98,
    "paymentMethod": "CREDIT_CARD",
    "customerId": "customer-1",
    "status": "AWAITING_PAYMENT"
  }
]
```

## Create Order

```http
POST /api/v1/orders
Content-Type: application/json
```

Request:

```json
{
  "reference": "ORD-42",
  "paymentMethod": "CREDIT_CARD",
  "customerId": "customer-1",
  "products": [
    {
      "productId": 5,
      "quantity": 2
    }
  ]
}
```

Response: `202 Accepted`

Header: `Location: /api/v1/orders/42`

```json
{
  "orderId": 42,
  "status": "PRODUCT_RESERVATION_PENDING"
}
```

Errors: `400 Bad Request` for validation failures or missing customer, `409 Conflict` when the order reference already exists with different input.

## Get Order

```http
GET /api/v1/orders/{id}
```

Response: `200 OK` with an order response.

Errors: `404 Not Found` when the order does not exist.

## Get Order Lines

```http
GET /api/v1/orders/{id}/order-lines
```

Response: `200 OK`

```json
[
  {
    "id": 1,
    "productId": 5,
    "productName": "Claw Hammer",
    "quantity": 2,
    "unitPrice": 18.99
  }
]
```

Use this endpoint with `GET /api/v1/orders/{id}` while polling the saga state. When order status is `AWAITING_PAYMENT`, fetch payment state from Payment Service and use its demo endpoints to confirm or fail payment.
