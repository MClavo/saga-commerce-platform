import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { EmptyState, ErrorState, TableSkeleton } from "@/components/shared/DataState"
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import type { OrderLineResponse, OrderResponse } from "@/features/orders/order-api"
import { amountsDiffer, getConfirmedLinesTotal, getOrderLineTotal, isPendingOrderAmount } from "@/features/orders/order-utils"
import { formatMoney } from "@/shared/formatters"
import type { ResourceState } from "@/shared/resource-state"

type OrderLinesCardProps = {
  order: OrderResponse
  state: ResourceState<OrderLineResponse>
}

export function OrderLinesCard({ order, state }: OrderLinesCardProps) {
  const confirmedLinesTotal = state.status === "success" ? getConfirmedLinesTotal(state.data) : null
  const hasMismatch = !isPendingOrderAmount(order) && amountsDiffer(confirmedLinesTotal, order.amount)

  return (
    <Card>
      <CardHeader>
        <CardTitle>Order lines</CardTitle>
        <CardDescription>Requested products and confirmed product snapshots when reservation succeeds.</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {hasMismatch ? (
          <Alert>
            <AlertTitle>Line total differs from order amount</AlertTitle>
            <AlertDescription>Confirmed product line totals do not match the current order amount.</AlertDescription>
          </Alert>
        ) : null}
        {state.status === "loading" ? <TableSkeleton columns={5} /> : null}
        {state.status === "error" ? <ErrorState message={state.error} title="Order lines unavailable" /> : null}
        {state.status === "success" && state.data.length === 0 ? (
          <EmptyState title="No order lines found." description="The order exists, but no requested products were returned." />
        ) : null}
        {state.status === "success" && state.data.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Product ID</TableHead>
                <TableHead>Product</TableHead>
                <TableHead>Quantity</TableHead>
                <TableHead>Unit price</TableHead>
                <TableHead>Line total</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {state.data.map((line) => {
                const lineTotal = getOrderLineTotal(line)

                return (
                  <TableRow key={line.id}>
                    <TableCell className="font-mono text-xs text-muted-foreground">#{line.productId}</TableCell>
                    <TableCell>{line.productName ?? "Snapshot pending"}</TableCell>
                    <TableCell className="font-mono">{line.quantity}</TableCell>
                    <TableCell className="font-mono">{line.unitPrice === null ? "not set" : formatMoney(line.unitPrice)}</TableCell>
                    <TableCell className="font-mono">{lineTotal === null ? "not set" : formatMoney(lineTotal)}</TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
            {confirmedLinesTotal !== null ? (
              <TableFooter>
                <TableRow>
                  <TableCell colSpan={4}>Confirmed lines total</TableCell>
                  <TableCell className="font-mono">{formatMoney(confirmedLinesTotal)}</TableCell>
                </TableRow>
              </TableFooter>
            ) : null}
          </Table>
        ) : null}
      </CardContent>
    </Card>
  )
}
