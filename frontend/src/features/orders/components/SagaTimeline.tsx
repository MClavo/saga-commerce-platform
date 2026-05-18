import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import type { OrderResponse } from "@/features/orders/order-api"
import type { PaymentResponse } from "@/features/payments/payment-api"
import { formatCompactDateTime } from "@/shared/formatters"
import type { BadgeVariant } from "@/shared/status-meta"

type TimelineState = "complete" | "active" | "pending" | "failed" | "skipped"

type TimelineItem = {
  title: string
  state: TimelineState
  detail: string
  timestamp?: string | null
}

type SagaTimelineProps = {
  order: OrderResponse
  payment: PaymentResponse | null
}

const stateMeta: Record<TimelineState, { label: string; variant: BadgeVariant }> = {
  complete: { label: "Complete", variant: "default" },
  active: { label: "Active", variant: "secondary" },
  pending: { label: "Pending", variant: "outline" },
  failed: { label: "Failed", variant: "destructive" },
  skipped: { label: "Skipped", variant: "outline" },
}

function buildTimeline(order: OrderResponse, payment: PaymentResponse | null): TimelineItem[] {
  const paymentResolved = payment?.status === "CONFIRMED" || payment?.status === "FAILED"

  if (order.status === "PRODUCT_RESERVATION_FAILED") {
    return [
      { title: "Order created", state: "complete", detail: "Order record exists.", timestamp: order.createdAt },
      { title: "Product reservation pending", state: "failed", detail: "Product reservation failed." },
      { title: "Product reserved", state: "skipped", detail: "No product snapshots were confirmed." },
      { title: "Payment pending", state: "skipped", detail: "No payment was requested." },
      { title: "Payment resolved", state: "skipped", detail: "Payment did not enter the saga." },
      { title: "Order resolved", state: "failed", detail: "Order completed with reservation failure." },
    ]
  }

  if (order.status === "PRODUCT_RESERVATION_PENDING") {
    return [
      { title: "Order created", state: "complete", detail: "Order record exists.", timestamp: order.createdAt },
      { title: "Product reservation pending", state: "active", detail: "Waiting for Product Service to reserve stock." },
      { title: "Product reserved", state: "pending", detail: "Product snapshot data is not confirmed yet." },
      { title: "Payment pending", state: "pending", detail: "Payment waits for product reservation." },
      { title: "Payment resolved", state: "pending", detail: "No payment outcome yet." },
      { title: "Order resolved", state: "pending", detail: "Order is not terminal." },
    ]
  }

  const paymentPendingState: TimelineState = payment ? "complete" : "active"
  const paymentResolvedState: TimelineState = paymentResolved ? "complete" : "pending"
  const orderResolvedState: TimelineState = order.status === "PAYMENT_FAILED" ? "failed" : order.status === "CONFIRMED" ? "complete" : "pending"
  const paymentResolvedDetail = payment?.status === "FAILED" ? "Payment failed." : payment?.status === "CONFIRMED" ? "Payment confirmed." : "No payment outcome yet."
  const orderResolvedDetail = order.status === "PAYMENT_FAILED" ? "Order completed with payment failure." : order.status === "CONFIRMED" ? "Order confirmed." : "Waiting for Order Service to finalize the order."

  return [
    { title: "Order created", state: "complete", detail: "Order record exists.", timestamp: order.createdAt },
    { title: "Product reservation pending", state: "complete", detail: "Reservation request was processed." },
    { title: "Product reserved", state: "complete", detail: "Product snapshots and confirmed prices are available." },
    {
      title: "Payment pending",
      state: paymentPendingState,
      detail: payment ? "Payment record exists." : "Waiting for Payment Service to create payment.",
      timestamp: payment?.createdAt,
    },
    {
      title: "Payment resolved",
      state: paymentResolvedState,
      detail: paymentResolvedDetail,
      timestamp: payment?.updatedAt,
    },
    { title: "Order resolved", state: orderResolvedState, detail: orderResolvedDetail },
  ]
}

export function SagaTimeline({ order, payment }: SagaTimelineProps) {
  const items = buildTimeline(order, payment)

  return (
    <Card>
      <CardHeader>
        <CardTitle>Saga timeline</CardTitle>
        <CardDescription>Inferred milestones from public order and payment state.</CardDescription>
      </CardHeader>
      <CardContent>
        <ol className="flex flex-col gap-3">
          {items.map((item) => {
            const meta = stateMeta[item.state]

            return (
              <li key={item.title} className="rounded-xl border p-3">
                <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                  <div className="flex flex-col gap-1">
                    <span className="font-medium">{item.title}</span>
                    <span className="text-sm text-muted-foreground">{item.detail}</span>
                    {item.timestamp ? <span className="text-xs text-muted-foreground">{formatCompactDateTime(item.timestamp)}</span> : null}
                  </div>
                  <Badge className="w-fit" variant={meta.variant}>
                    {meta.label}
                  </Badge>
                </div>
              </li>
            )
          })}
        </ol>
      </CardContent>
    </Card>
  )
}
