import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
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
import type { ResourceState } from "@/features/dashboard/use-dashboard-data"
import { formatCompactDateTime, formatMoney } from "@/shared/formatters"
import { formatRequiredRoles } from "@/shared/role-labels"
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
        {state.status === "restricted" ? <Restricted roles={state.roles} /> : null}
        {state.status === "error" ? <p className="text-sm text-destructive">{state.error}</p> : null}
        {state.status === "success" && orders.length === 0 ? <Empty label="No orders have been created yet." /> : null}
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

function Restricted({ roles }: { roles: string[] }) {
  return <p className="text-sm text-muted-foreground">Restricted: requires {formatRequiredRoles(roles)}.</p>
}

function Empty({ label }: { label: string }) {
  return <p className="rounded-lg bg-muted p-4 text-sm text-muted-foreground">{label}</p>
}

function TableSkeleton({ columns }: { columns: number }) {
  return (
    <div className="flex flex-col gap-2">
      {Array.from({ length: 5 }).map((_, row) => (
        <div key={row} className="grid gap-2" style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}>
          {Array.from({ length: columns }).map((__, column) => (
            <Skeleton key={column} className="h-7" />
          ))}
        </div>
      ))}
    </div>
  )
}
