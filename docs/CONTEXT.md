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

## Product

A catalog item with price, category, and available stock. Product stock is visible because order processing depends on product reservation.
