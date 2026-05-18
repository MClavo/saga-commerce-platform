import { Button } from "@/components/ui/button"
import { EmptyState, ErrorState, RestrictedState, TableSkeleton } from "@/components/shared/DataState"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { StatusBadge } from "@/components/shared/StatusBadge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import type { OrderResponse } from "@/features/orders/order-api"
import type { ResourceState } from "@/shared/resource-state"
import { formatCompactDateTime, formatMoney } from "@/shared/formatters"
import { getOrderStatusMeta } from "@/shared/status-meta"

type LatestOrdersCardProps = {
  state: ResourceState<OrderResponse>
  orders: OrderResponse[]
  hasMore: boolean
}

export function LatestOrdersCard({ state, orders, hasMore }: LatestOrdersCardProps) {
  return (
    <Card>
      <CardHeader>
        <div>
          <CardTitle>Latest orders</CardTitle>
          <CardDescription>Newest saga entries from Order Service.</CardDescription>
        </div>
        {hasMore ? (
          <CardAction>
            <Button disabled size="sm" type="button" variant="outline">
              View all
            </Button>
          </CardAction>
        ) : null}
      </CardHeader>
      <CardContent>
        {state.status === "loading" ? <TableSkeleton columns={5} /> : null}
        {state.status === "restricted" ? <RestrictedState roles={state.roles} /> : null}
        {state.status === "error" ? <ErrorState compact message={state.error} /> : null}
        {state.status === "success" && orders.length === 0 ? <EmptyState compact title="No orders have been created yet." /> : null}
        {state.status === "success" && orders.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Reference</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Customer</TableHead>
                <TableHead>Created</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {orders.map((order) => (
                <TableRow key={order.id}>
                  <TableCell className="font-medium">{order.reference}</TableCell>
                  <TableCell>
                    <StatusBadge meta={getOrderStatusMeta(order.status)} status={order.status} />
                  </TableCell>
                  <TableCell className="font-mono">{formatMoney(order.amount)}</TableCell>
                  <TableCell className="font-mono text-xs text-muted-foreground">{order.customerId}</TableCell>
                  <TableCell>{formatCompactDateTime(order.createdAt)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : null}
      </CardContent>
    </Card>
  )
}
