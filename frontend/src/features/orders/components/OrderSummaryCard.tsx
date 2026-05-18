import { DetailItem } from "@/components/shared/DetailItem"
import { StatusBadge } from "@/components/shared/StatusBadge"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import type { OrderResponse } from "@/features/orders/order-api"
import { formatOrderAmount, formatPaymentMethod } from "@/features/orders/order-utils"
import { formatCompactDateTime, formatMoney } from "@/shared/formatters"
import { getOrderStatusMeta } from "@/shared/status-meta"

type OrderSummaryCardProps = {
  order: OrderResponse
}

export function OrderSummaryCard({ order }: OrderSummaryCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Order summary</CardTitle>
        <CardDescription>Business record and current public order state.</CardDescription>
      </CardHeader>
      <CardContent className="grid gap-3">
        <div className="rounded-xl border p-3">
          <span className="text-xs text-muted-foreground">Status</span>
          <div className="mt-2">
            <StatusBadge meta={getOrderStatusMeta(order.status)} status={order.status} />
          </div>
        </div>
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
          <DetailItem label="Reference" value={order.reference} />
          <DetailItem label="Order ID" value={`#${order.id}`} mono />
          <DetailItem label="Customer ID" value={order.customerId} mono />
          <DetailItem label="Amount" value={formatOrderAmount(order, "Pending price confirmation") ?? formatMoney(order.amount)} mono />
          <DetailItem label="Payment method" value={formatPaymentMethod(order.paymentMethod)} />
          <DetailItem label="Created" value={formatCompactDateTime(order.createdAt)} />
        </div>
      </CardContent>
    </Card>
  )
}
