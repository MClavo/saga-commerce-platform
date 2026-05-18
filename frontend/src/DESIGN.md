# Frontend Structure

## Decision Priority
- When two implementations overlap, keep the one closest to this order: dashboard, products/catalog, customers.
- Dashboard defines the default visual language for page headers, operational cards, table states, and compact status display.
- Products/catalog can add domain-specific inventory patterns when dashboard has no equivalent.
- Customers should reuse shared patterns unless its domain validation requires a local implementation.

## Folders
- `app/`: router and application composition.
- `components/ui/`: primitive shadcn/Radix building blocks only. Do not place domain logic here.
- `components/shared/`: reusable application components with no single-feature ownership.
- `features/<name>/`: feature data access, hooks, domain utilities, and feature-only components.
- `features/auth/`: auth state, guards, auth API, auth pages, and auth-owned UI.
- `shared/`: cross-feature types, formatters, status metadata, local constants, and state helpers.

## Reuse Rules
- Prefer shared components for page headers, metric tiles, detail tiles, loading skeletons, empty states, errors, and restricted states.
- Keep API functions and domain parsing/validation in their feature folders.
- Extract only repeated structure. Do not hide domain-specific table columns or form validation behind generic abstractions.
- Use `components/ui` directly for low-level composition; use `components/shared` for repeated product/dashboard/customer application patterns.

## Styling
- Preserve the existing Geist/zinc dashboard-oriented style.
- Prefer Tailwind composition in shared components over global CSS utilities.
- Use `min-h-[100dvh]` for full viewport pages.
- Keep status codes in mono text and numeric values in mono text.
