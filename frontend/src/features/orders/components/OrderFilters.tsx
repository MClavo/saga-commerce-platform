import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { orderStatusOptions } from "@/features/orders/order-utils"
import type { OrderStatus } from "@/shared/status-meta"

export type OrderStatusFilter = OrderStatus | "ALL"

type OrderFiltersProps = {
  search: string
  status: OrderStatusFilter
  onSearchChange: (value: string) => void
  onStatusChange: (value: OrderStatusFilter) => void
  onReset: () => void
}

export function OrderFilters({ search, status, onSearchChange, onStatusChange, onReset }: OrderFiltersProps) {
  const hasFilters = Boolean(search.trim()) || status !== "ALL"

  return (
    <Card>
      <CardHeader>
        <div>
          <CardTitle>Order search</CardTitle>
          <CardDescription>Local search across reference, order ID, and customer ID.</CardDescription>
        </div>
        <CardAction>
          <Button type="button" variant="outline" onClick={onReset} disabled={!hasFilters}>
            Reset
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent>
        <FieldGroup className="md:grid md:grid-cols-[1.4fr_0.8fr] md:items-start">
          <Field>
            <FieldLabel htmlFor="order-search">Search orders</FieldLabel>
            <Input
              id="order-search"
              value={search}
              placeholder="Reference, order ID, or customer ID"
              onChange={(event) => onSearchChange(event.target.value)}
            />
            <FieldDescription>Uses loaded order records only; no server-side query.</FieldDescription>
          </Field>

          <Field>
            <FieldLabel htmlFor="order-status-filter">Status</FieldLabel>
            <select
              id="order-status-filter"
              className="h-8 rounded-lg border border-input bg-background px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
              value={status}
              onChange={(event) => onStatusChange(event.target.value as OrderStatusFilter)}
            >
              {orderStatusOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <FieldDescription>Filters by exact public order status.</FieldDescription>
          </Field>
        </FieldGroup>
      </CardContent>
    </Card>
  )
}
