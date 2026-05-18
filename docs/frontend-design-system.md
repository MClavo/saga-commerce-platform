# Frontend Design System

## Source Of Truth

This document captures UI coherence rules for the demo frontend. The product/page roadmap remains in `docs/frontend-demo-design.md`.

## Product Character

The frontend is an Operations Dashboard / Admin Demo Console. It should make backend architecture visible through real resource state, clear role boundaries, and explicit saga terminology.

Avoid consumer storefront patterns, marketing sections, fake analytics, and decorative checkout flows.

## Layout

- Use a compact top application bar for primary navigation, Local Dev Tools, and the current user menu.
- Keep `Dashboard` as the only active app route until additional pages are implemented.
- Roadmap navigation items may be visible as disabled labels when they communicate planned scope.
- Use responsive single-column layouts below `md`.
- Prefer asymmetric grid composition on desktop when it improves scanning, but keep operational readability above novelty.

## Navigation

Primary app navigation for the current phase:

- Dashboard: active route.
- Catalog: active route.
- Customers: disabled roadmap item.
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
