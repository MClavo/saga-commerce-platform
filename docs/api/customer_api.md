# Customer API

Base path: `/api/v1/customers`

Gateway access: all customer endpoints require `ROLE_CUSTOMER_SUPPORT` or `ROLE_ADMIN`.

## Customer Request

Used by create and update.

```json
{
  "id": "customer-1",
  "firstname": "Jane",
  "lastname": "Doe",
  "email": "jane.doe@example.com",
  "address": {
    "id": "address-1",
    "street": "Main Street",
    "houseNumber": "42A",
    "zipCode": "10001"
  }
}
```

Required fields: `firstname`, `lastname`, `email`.

Validation: `email` must be a valid email address.

Optional fields: `id`, `address`, and all address fields.

## Customer Response

```json
{
  "id": "customer-1",
  "firstname": "Jane",
  "lastname": "Doe",
  "email": "jane.doe@example.com",
  "address": {
    "id": "address-1",
    "street": "Main Street",
    "houseNumber": "42A",
    "zipCode": "10001"
  }
}
```

`address` may be `null`.

## List Customers

```http
GET /api/v1/customers
```

Response: `200 OK`

```json
[
  {
    "id": "customer-1",
    "firstname": "Jane",
    "lastname": "Doe",
    "email": "jane.doe@example.com",
    "address": {
      "id": "address-1",
      "street": "Main Street",
      "houseNumber": "42A",
      "zipCode": "10001"
    }
  }
]
```

## Create Customer

```http
POST /api/v1/customers
Content-Type: application/json
```

Request: customer request.

Response: `200 OK` with the created customer response.

Errors: `400 Bad Request` for validation failures.

## Update Customer

```http
PUT /api/v1/customers/{id}
Content-Type: application/json
```

Request: customer request.

Response: `202 Accepted` with no body.

Notes: non-blank `firstname`, `lastname`, and `email` replace existing values. If `address` is provided, it replaces the existing address.

Errors: `400 Bad Request` for validation failures, `404 Not Found` when the customer does not exist.

## Get Customer

```http
GET /api/v1/customers/{id}
```

Response: `200 OK` with a customer response.

Errors: `404 Not Found` when the customer does not exist.

## Check Customer Exists

```http
GET /api/v1/customers/exists/{id}
```

Response: `200 OK`

```json
true
```

Returns `true` when a customer with `{id}` exists, otherwise `false`.

## Delete Customer

```http
DELETE /api/v1/customers/{id}
```

Response: `202 Accepted` with no body.

The current service accepts deletion requests even when the ID does not exist.
