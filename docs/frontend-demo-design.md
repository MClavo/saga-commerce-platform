# Frontend Demo Design Guide

## Purpose

This frontend should be a **technical demo console**, not a generic consumer e-commerce storefront.

The goal is to make the backend architecture visible and easy to evaluate:

- customer validation
- product selection
- order creation
- asynchronous product reservation
- payment inspection
- demo payment confirmation/failure
- final order state resolution
- operational visibility over products, customers, orders, and payments

The frontend should help recruiters and technical reviewers understand the distributed workflow quickly.

## Product Positioning

Build the UI as an **Operations Dashboard / Admin Demo Console**.

Avoid spending too much effort on B2C storefront features such as wishlists, reviews, persistent carts, marketing pages, or decorative checkout flows. The backend is the differentiator; the frontend should expose and explain it.

Recommended visual direction:

- Linear-style dashboard
- Stripe-style admin console
- Vercel-style clean layout
- Retool-like operational UI

## Core UX Principle

The main value of the frontend is to let a reviewer see the full order-processing saga from start to finish.

The ideal demo flow should be easy to record as a short GIF or video:

1. Open the dashboard.
2. Start a saga demo.
3. Select a customer.
4. Select one or more products.
5. Create an order.
6. Observe `PRODUCT_RESERVATION_PENDING`.
7. Observe transition to `AWAITING_PAYMENT`.
8. Inspect the related payment in `PENDING` status.
9. Confirm or fail the payment using demo actions.
10. Observe final order state: `CONFIRMED` or `PAYMENT_FAILED`.

## Recommended Pages

### 1. Dashboard

Purpose: provide a high-level overview of the system.

Content:

- summary cards for Products, Customers, Orders, and Payments
- latest orders
- latest payments
- quick action: `Start Saga Demo`
- compact explanation of the saga flow

Avoid fake metrics. Prefer simple real counts derived from list endpoints.

### 2. Catalog / Products

Purpose: inspect and manage products.

Content:

- product list or product grid
- product detail view
- available stock
- price
- category
- stock status badge
- admin-only create/update actions
- admin-only stock adjustment action

Important design point: stock should be visible because product reservation is part of the saga.

Suggested UI:

- table or card grid
- category filter
- search input
- stock badges: `In Stock`, `Low Stock`, `Out of Stock`
- stock adjustment dialog for admin users

### 3. Customers

Purpose: simple customer management and customer selection support.

Content:

- customer list
- create customer
- update customer
- customer detail
- delete customer

This page is useful but should not dominate the demo. Keep it clean and functional.

Suggested UI:

- table
- local search
- detail drawer
- create/update dialog

### 4. Orders

Purpose: inspect orders and access the saga detail view.

#### Orders List

Show:

- order id
- reference
- customer id
- amount
- payment method
- status badge
- action: `View Flow`

Order statuses:

- `PRODUCT_RESERVATION_PENDING`
- `AWAITING_PAYMENT`
- `CONFIRMED`
- `PRODUCT_RESERVATION_FAILED`
- `PAYMENT_FAILED`

The status badge design must be very clear because order state is central to the demo.

#### Order Detail / Saga Detail

This is the most important screen in the frontend.

Show:

- order summary
- order lines
- current order status
- related payment state
- saga timeline
- available demo actions

Suggested timeline:

```text
Order Created
↓
Product Reservation Pending
↓
Product Reserved
↓
Payment Pending
↓
Payment Confirmed / Failed
↓
Order Confirmed / Payment Failed
```

The backend does not need to expose a full event history for the first version. The UI can infer timeline progress from:

- order status
- payment status
- existence of order lines
- available product snapshot data

### 5. Saga Demo

Purpose: guided flow to create and observe an order-processing saga.

This should be a dedicated feature because it crosses several domains: customers, products, orders, and payments.

Recommended wizard:

#### Step 1: Select Customer

- list or combobox of customers
- show name and email

#### Step 2: Select Products

- product table or cards
- quantity selector
- available stock
- unit price
- subtotal

#### Step 3: Select Payment Method

Allowed values:

- `CREDIT_CARD`
- `DEBIT_CARD`
- `PAYPAL`
- `VISA`
- `MASTERCARD`

#### Step 4: Create Order

After order creation, redirect immediately to the saga detail page.

Recommended route:

```text
/orders/:id/flow
```

Do not return the user to a generic list after creating the order. The main point is to watch the asynchronous workflow.

### 6. Payments

A standalone Payments page is optional.

Recommended approach:

- Keep payment inspection available in the order flow screen.
- Add a standalone Payments page only if it improves clarity.

Payment demo actions should live primarily inside the order flow screen:

- `Confirm Payment`
- `Fail Payment`

Show these actions only when:

- order status is `AWAITING_PAYMENT`
- payment status is `PENDING`
- the current user has admin permissions

## Navigation

Recommended primary navigation:

```text
Dashboard
Catalog
Customers
Orders
Saga Demo
```

Optional:

```text
Payments
Admin
```

Do not overcomplicate navigation. The demo path should always be obvious.

## Feature Structure

The current feature-based structure is appropriate:

```text
src/
  app/
  assets/
  components/
  features/
    customers/
    orders/
    payments/
    products/
  lib/
  shared/
  index.css
  main.tsx
```

Recommended additions:

```text
features/
  dashboard/
  saga-demo/
```

Suggested responsibility split:

- `features/products`: product listing, detail, create/update, stock adjustment
- `features/customers`: customer CRUD and customer selection components
- `features/orders`: order list, order detail, order lines, order status display
- `features/payments`: payment API access, payment status components, demo payment actions
- `features/saga-demo`: cross-domain guided workflow
- `features/dashboard`: overview and quick access

Do not place the whole demo workflow inside `orders`. The saga demo is a cross-feature UI flow and should remain separate.

## UI Components

Recommended shadcn components:

- `Card`
- `Badge`
- `Table`
- `Tabs`
- `Dialog`
- `Sheet`
- `Command`
- `Skeleton`
- `Toast`
- `Alert`
- `DropdownMenu`

Custom components worth creating:

- `StatusBadge`
- `SagaTimeline`
- `MoneyText`
- `EmptyState`
- `PageHeader`
- `ResourceTable`
- `RoleGuard`
- `PollingIndicator`
- `OrderStatusBadge`
- `PaymentStatusBadge`

## State and Data Fetching

Recommended behavior:

- Use query-based data fetching for list/detail endpoints.
- Use mutations for create/update/delete/demo actions.
- Poll the order detail while the saga is not in a terminal state.
- Poll payment-by-order only when the order reaches `AWAITING_PAYMENT`.
- Stop polling when the order reaches a terminal status.

Terminal order statuses:

- `CONFIRMED`
- `PRODUCT_RESERVATION_FAILED`
- `PAYMENT_FAILED`

Avoid global state unless necessary. Most state should come from server queries and route params.

## Error Handling

Error states should be explicit and useful.

Examples:

- customer missing during order creation
- insufficient stock / reservation failure
- duplicate order reference conflict
- payment not found while waiting for async creation
- payment already confirmed or failed
- unauthorized admin action

Use clear alerts or toasts. Do not hide backend errors behind generic messages.

## Role-Based UI

The UI should reflect backend access rules:

- product GET endpoints are public
- product mutations require admin
- customer endpoints require customer support or admin
- order GET endpoints require order manager or admin
- order creation requires authenticated JWT
- payment GET endpoints require order manager or admin
- payment confirm/fail demo endpoints require admin

Role-based UI should hide or disable unavailable actions, but the backend remains the source of truth.

## Design Priorities

Prioritize:

- clear states
- readable tables
- useful loading states
- explicit empty states
- saga timeline
- admin/demo actions
- fast demo flow
- screenshots that explain the backend

Deprioritize:

- complex cart behavior
- marketing pages
- fake analytics
- heavy animations
- wishlist/reviews
- overly polished B2C checkout
- excessive mobile optimization in the first version

## Portfolio Value

This frontend should prove that the system is not just a set of isolated CRUD services.

It should demonstrate:

- distributed workflow visibility
- asynchronous consistency
- operational thinking
- role-aware UI
- clean feature boundaries
- production-style error handling
- backend-driven state transitions

The frontend should act as a window into the architecture.
