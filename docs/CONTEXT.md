# Context Glossary

## Authenticated User

A person signed in through the gateway-managed browser session. An authenticated user may have roles such as Admin, Order Manager, or Customer Support.

## Customer

A business entity that can be validated and assigned to an order. A customer is distinct from the authenticated user operating the demo console.

## Dashboard

The authenticated home of the demo console. It summarizes real backend resources and exposes the intended operational scope of the system.

## Local Dev Tools

Local browser interfaces for inspecting demo infrastructure and operations. They support development and evaluation, not customer-facing workflows.

## Order Processing Saga

The distributed order workflow that validates a customer, reserves products, requests payment, resolves final order state, and triggers notification.

## Order

A business record representing a customer purchase request. It captures the products requested by a customer and tracks the order lifecycle from creation through successful completion or failure.

## Payment

A business record representing an attempt to settle the financial obligation associated with an order. A payment may succeed or fail, and its outcome influences the final state of the order.

## Product

A catalog item with price, category, and available stock. Product stock is visible because order processing depends on product reservation.
