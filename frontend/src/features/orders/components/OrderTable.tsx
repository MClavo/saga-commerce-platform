import { Link } from "react-router-dom"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { EmptyState, ErrorState, TableSkeleton } from "@/components/shared/DataState"
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
import { formatOrderAmount, formatPaymentMethod } from "@/features/orders/order-utils"
import { formatCompactDateTime, formatMoney } from "@/shared/formatters"
import type { ResourceState } from "@/shared/resource-state"
import { getOrderStatusMeta } from "@/shared/status-meta"

type OrderTableProps = {
  state: ResourceState<OrderResponse>
  orders: OrderResponse[]
  totalCount: number
}

export function OrderTable({ state, orders, totalCount }: OrderTableProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Orders</CardTitle>
        <CardDescription>
          {state.status === "success" ? `Showing ${orders.length} of ${totalCount} loaded order records.` : "Order records from Order Service."}
        </CardDescription>
      </CardHeader>
      <CardContent>
        {state.status === "loading" ? <TableSkeleton columns={8} /> : null}
        {state.status === "error" ? <ErrorState message={state.error} title="Orders unavailable" /> : null}
        {state.status === "success" && totalCount === 0 ? (
          <EmptyState title="No orders have been created yet." description="Orders will appear after the saga demo or API creates one." />
        ) : null}
        {state.status === "success" && totalCount > 0 && orders.length === 0 ? (
          <EmptyState title="No orders match the current filters." description="Reset search or status filters to inspect all loaded orders." />
        ) : null}
        {state.status === "success" && orders.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Order</TableHead>
                <TableHead>Reference</TableHead>
                <TableHead>Customer</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Payment</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {orders.map((order) => (
                <TableRow key={order.id}>
                  <TableCell className="font-mono text-xs text-muted-foreground">#{order.id}</TableCell>
                  <TableCell className="font-medium">{order.reference}</TableCell>
                  <TableCell className="font-mono text-xs text-muted-foreground">{order.customerId}</TableCell>
                  <TableCell className="font-mono">
                    {formatOrderAmount(order) ?? formatMoney(order.amount)}
                  </TableCell>
                  <TableCell>{formatPaymentMethod(order.paymentMethod)}</TableCell>
                  <TableCell>
                    <StatusBadge meta={getOrderStatusMeta(order.status)} status={order.status} />
                  </TableCell>
                  <TableCell>{formatCompactDateTime(order.createdAt)}</TableCell>
                  <TableCell>
                    <Button asChild size="sm" variant="outline">
                      <Link to={`/orders/${order.id}/flow`}>View Flow</Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : null}
      </CardContent>
    </Card>
  )
}
