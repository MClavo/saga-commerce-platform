# Product API

Base path: `/api/v1/products`

Product reservation and purchase are saga-driven through Kafka. There is no public `/purchase` endpoint.

Gateway access: `GET` endpoints are public. `POST`, `PUT`, `PATCH`, and `DELETE` require `ROLE_ADMIN`.

## Product Response

```json
{
  "id": 1,
  "name": "Claw Hammer",
  "description": "16 oz claw hammer with fiberglass handle for general carpentry.",
  "availableQuantity": 42,
  "price": 18.99,
  "categoryId": 1,
  "categoryName": "Hand Tools",
  "categoryDescription": "Hand tools for manual work, fastening, cutting, and assembly."
}
```

## List Products

```http
GET /api/v1/products
```

Response: `200 OK`

```json
[
  {
    "id": 1,
    "name": "Claw Hammer",
    "description": "16 oz claw hammer with fiberglass handle for general carpentry.",
    "availableQuantity": 42,
    "price": 18.99,
    "categoryId": 1,
    "categoryName": "Hand Tools",
    "categoryDescription": "Hand tools for manual work, fastening, cutting, and assembly."
  }
]
```

## Get Product

```http
GET /api/v1/products/{id}
```

Response: `200 OK` with a product response.

Errors: `404 Not Found` when the product does not exist.

## Create Product

```http
POST /api/v1/products
Content-Type: application/json
```

Request:

```json
{
  "name": "Claw Hammer",
  "description": "16 oz claw hammer with fiberglass handle for general carpentry.",
  "availableQuantity": 42,
  "price": 18.99,
  "categoryId": 1
}
```

Response: `200 OK` with the created product ID.

```json
1
```

Errors: `400 Bad Request` for validation failures.

## Update Product Catalog Fields

```http
PUT /api/v1/products/{id}
Content-Type: application/json
```

Updates catalog fields only. This endpoint does not change `availableQuantity` or `reservedQuantity`.

Request:

```json
{
  "name": "Updated Hammer",
  "description": "Updated product description.",
  "price": 24.99,
  "categoryId": 1
}
```

Response: `200 OK` with the updated product response.

Errors: `400 Bad Request` for validation failures, `404 Not Found` when the product does not exist.

## Adjust Available Stock

```http
POST /api/v1/products/{id}/stock-adjustments
Content-Type: application/json
```

Applies a delta to `availableQuantity`. Positive values add stock. Negative values remove available stock. The delta must be nonzero and the final `availableQuantity` cannot be negative. This endpoint does not change `reservedQuantity`.

Request:

```json
{
  "quantityDelta": -3
}
```

Response: `200 OK` with the updated product response.

Errors: `400 Bad Request` for validation failures or invalid stock deltas, `404 Not Found` when the product does not exist.
