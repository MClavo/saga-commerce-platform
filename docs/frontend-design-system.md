# Frontend Design System

## Source Of Truth

This document captures UI coherence rules for the demo frontend. The product/page roadmap remains in `docs/frontend-demo-design.md`.

## Product Character

The frontend is an Operations Dashboard / Admin Demo Console. It should make backend architecture visible through real resource state, clear role boundaries, and explicit saga terminology.

Avoid consumer storefront patterns, marketing sections, fake analytics, and decorative checkout flows.

## Layout

- Use a compact top application bar for primary navigation, Local Dev Tools, and the current user menu.
- Keep implemented pages as active routes and planned pages as disabled roadmap labels.
- Roadmap navigation items may be visible as disabled labels when they communicate planned scope.
- Use responsive single-column layouts below `md`.
- Prefer asymmetric grid composition on desktop when it improves scanning, but keep operational readability above novelty.

## Navigation

Primary app navigation for the current phase:

- Dashboard: active route.
- Catalog: active route.
- Customers: active route, restricted to Customer Support/Admin.
- Orders: disabled roadmap item.
- Saga Demo: disabled roadmap item.

Local Dev Tools are separate from product navigation. They link only to localhost browser UIs for local infrastructure and must open in a new tab.

## Visual Language

- Use Geist sans-serif typography already configured in the frontend.
- Keep surfaces neutral and technical.
- Use at most one accent treatment per section.
- Avoid neon glows, saturated gradients, emojis, and decorative fake metrics.
- Use shadcn/ui components as structured primitives, with layout and hierarchy tailored to this console.

## Cards And Tables

- Use cards for meaningful grouping, not for every small piece of content.
- Dashboard summary cards show exactly one primary count and one secondary real detail.
- Tables must stay compact, scan-friendly, and horizontally safe on small screens.
- Latest dashboard tables show at most five records and expose a disabled future `View all` action when more data exists.

## Data Rules

- Prefer real counts derived from list endpoints.
- Do not invent trends, percentages, conversion rates, or analytics.
- Skip role-restricted API calls when the current user lacks the required role.
- Show restricted states inline with friendly required role names.
- Do not show expected authorization restrictions as errors or toasts.

## Formatting

- Money uses a frontend demo EUR convention until the API exposes currency explicitly.
- Render money as `37,98 €`, using comma decimals and dot thousands separators.
- Dates use compact local European ordering for display.
- Sorting uses raw ISO date-time values, not localized display strings.

## Status Display

Statuses are central to the demo and must preserve backend vocabulary.

- Show a human-readable label inside a badge.
- Show the raw enum as muted monospace text beside or below the badge.
- Use semantic variants: active states are secondary, successful terminal states are default, failed terminal states are destructive.

## Role Language

Use friendly role labels in UI copy:

- `ROLE_ADMIN` -> Admin
- `ROLE_ORDER_MANAGER` -> Order Manager
- `ROLE_CUSTOMER_SUPPORT` -> Customer Support

Role checks still use exact backend role values.

## Loading, Empty, And Error States

- Loading states should match the eventual layout using skeletons.
- Empty states should explain what resource is missing and why it matters.
- Error states should be inline and specific enough to identify the affected resource.
- Use manual refresh on overview pages; reserve polling for saga/order flow screens.

## Dashboard Contract

The dashboard includes:

- Summary cards for Products, Customers, Orders, and Payments.
- Latest orders, sorted by descending `createdAt`, with descending `id` fallback.
- Latest payments, sorted by descending `createdAt`, with descending `id` fallback.
- Compact saga flow explanation.
- Disabled `Start Saga Demo` action until the saga demo page is implemented.
- Local Dev Tools launcher with Keycloak, Zipkin, MailDev, pgAdmin, and Mongo Express links.

## Catalog Contract

The Catalog page includes:

- Product table as the primary view.
- Search by product name, description, or category.
- Category filter derived from loaded products.
- Stock filter for All, In Stock, Low Stock, and Out Of Stock.
- Product detail sheet opened from table actions.
- Admin-only create, edit, and stock adjustment actions.
- Disabled mutation controls for non-admin users with clear Admin role language.

Stock badge thresholds:

- Out Of Stock: `availableQuantity <= 0`
- Low Stock: `availableQuantity > 0 && availableQuantity <= 5`
- In Stock: `availableQuantity > 5`

Catalog mutations remain backend-authorized. The UI mirrors role rules but does not replace backend enforcement.

## Customers Contract

The Customers page includes:

- Protected route requiring Customer Support or Admin.
- Header restriction label: `Restricted: Customer Support / Admin`.
- Customer table as the primary view.
- Local search by full name, first name, last name, email, customer ID, and address summary.
- Alphabetical sort by last name, first name, then email.
- Real metrics for Total customers, With address, and Missing address.
- Detail sheet opened from table actions.
- Create and update dialog for first name, last name, email, and optional embedded address.
- Delete confirmation dialog with hard-delete warning.

Customer address display:

- Address is an embedded value object with street, house number, and zip code.
- If address exists, render compactly as `Main Street 12, 29001`.
- If address is null or all parts are blank, render `No address`.
- Partial addresses omit missing pieces.

Customer mutations remain backend-authorized. The UI mirrors role rules but does not replace backend enforcement.

## Orders Contract

The Orders page includes:

- Protected route requiring Order Manager or Admin.
- Top navigation link visible to all authenticated users; route guards enforce access.
- Header refresh action only.
- Order table as the primary view.
- Local search by reference, order ID, and customer ID.
- Exact status filter for All plus the public order statuses.
- Newest-first sorting by `createdAt`, with descending `id` fallback.
- Real metrics for Total orders, Active sagas, Awaiting payment, and Failed orders.
- Explicit `View Flow` table action; list rows are not clickable.

The Order Flow page includes:

- Route `/orders/:id/flow`.
- Page title `Order Flow`.
- Header actions for Back to Orders and Refresh.
- Desktop layout with saga timeline as the main column and order summary/payment state as the side column.
- Manual refresh plus polling indicator.
- Inline errors only; no success toasts.

Order Flow polling rules:

- Poll order state every 2 seconds while the order is not terminal.
- Stop order polling when status is `CONFIRMED`, `PRODUCT_RESERVATION_FAILED`, or `PAYMENT_FAILED`.
- Fetch payment state only for `AWAITING_PAYMENT`, `CONFIRMED`, and `PAYMENT_FAILED`.
- Treat missing payment during `AWAITING_PAYMENT` as a waiting state.
- Treat missing payment for terminal payment outcomes as an inconsistency.

Order Flow timeline milestones:

- Order created.
- Product reservation pending.
- Product reserved.
- Payment pending.
- Payment resolved.
- Order resolved.

Timeline statuses are inferred from public order and payment statuses. The UI must not expose internal-only order statuses as current order state.

Payment demo actions:

- Live in the payment panel on the Order Flow page.
- Available only when order status is `AWAITING_PAYMENT` and payment status is `PENDING`.
- Admin users can confirm payment directly.
- Admin users must confirm before failing payment.
- Non-admin users see disabled controls with Admin-required context.

Order lines display:

- Always show requested product ID and quantity.
- Before product snapshot data arrives, show pending placeholders for product name, unit price, and line total.
- After reservation succeeds, show confirmed product name, unit price, line total, and confirmed lines total.
- Show non-blocking warnings when confirmed line totals or payment amounts differ from the order amount after rounding to cents.
