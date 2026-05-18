import { useEffect, useRef } from "react"
import { Link, useParams } from "react-router-dom"
import { toast } from "sonner"

import { AppShell } from "@/components/layout/AppShell"
import { EmptyState, ErrorState, TableSkeleton } from "@/components/shared/DataState"
import { PageHeader } from "@/components/shared/PageHeader"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { OrderLinesCard } from "@/features/orders/components/OrderLinesCard"
import { OrderSummaryCard } from "@/features/orders/components/OrderSummaryCard"
import { PaymentPanel } from "@/features/orders/components/PaymentPanel"
import { SagaTimeline } from "@/features/orders/components/SagaTimeline"
import { useAuth } from "@/features/auth/use-auth"
import { isTerminalOrderStatus } from "@/features/orders/order-utils"
import { useOrderFlowData } from "@/features/orders/use-order-flow-data"
import type { OrderStatus } from "@/shared/status-meta"
import { localDevTools } from "@/shared/local-dev-tools"

const mailDevTool = localDevTools.find((tool) => tool.name === "MailDev")

function openMailDev() {
  if (!mailDevTool) {
    return
  }

  window.open(mailDevTool.href, "_blank", "noopener,noreferrer")
}

function notifyFlowFinished(reference: string, status: OrderStatus) {
  const baseOptions = {
    id: `order-flow-finished-${reference}`,
    description: <span className="text-foreground">Order flow reached final state. Notification email should now be available in MailDev.</span>,
    action: mailDevTool
      ? {
          label: "Open MailDev",
          onClick: openMailDev,
        }
      : undefined,
  }

  if (status === "CONFIRMED") {
    toast.success(`Order ${reference} confirmed.`, baseOptions)
    return
  }

  if (status === "PRODUCT_RESERVATION_FAILED") {
    toast.error(`Order ${reference} ended: reservation failed.`, baseOptions)
    return
  }

  toast.error(`Order ${reference} ended: payment failed.`, baseOptions)
}

function parseOrderId(value: string | undefined) {
  if (!value) {
    return null
  }

  const parsed = Number(value)

  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

export function OrderFlowPage() {
  const { id } = useParams()
  const orderId = parseOrderId(id)
  const { hasRole } = useAuth()
  const {
    order,
    orderLines,
    payment,
    isRefreshing,
    isPolling,
    actionError,
    refresh,
    confirmPayment,
    failPayment,
    isConfirmingPayment,
    isFailingPayment,
  } = useOrderFlowData(orderId)
  const previousOrderStatusRef = useRef<OrderStatus | null>(null)

  useEffect(() => {
    previousOrderStatusRef.current = null
  }, [orderId])

  useEffect(() => {
    if (order.status !== "success") {
      return
    }

    const currentStatus = order.data.status
    const previousStatus = previousOrderStatusRef.current

    if (previousStatus && previousStatus !== currentStatus && !isTerminalOrderStatus(previousStatus) && isTerminalOrderStatus(currentStatus)) {
      notifyFlowFinished(order.data.reference, currentStatus)
    }

    previousOrderStatusRef.current = currentStatus
  }, [order])

  const headerActions = (
    <>
      <Button asChild type="button" variant="outline">
        <Link to="/orders">Back to Orders</Link>
      </Button>
      <Button type="button" variant="outline" onClick={() => void refresh()} disabled={isRefreshing}>
        {isRefreshing ? "Refreshing" : "Refresh"}
      </Button>
    </>
  )

  if (order.status === "loading") {
    return (
      <AppShell>
        <div className="flex flex-col gap-6">
          <PageHeader
            eyebrow="Orders"
            title="Order Flow"
            description="Loading order flow state."
            badge={<Badge variant="secondary">Polling every 2s</Badge>}
            actions={headerActions}
          />
          <Card>
            <CardHeader>
              <CardTitle>Loading flow</CardTitle>
            </CardHeader>
            <CardContent>
              <TableSkeleton columns={3} rows={4} />
            </CardContent>
          </Card>
        </div>
      </AppShell>
    )
  }

  if (order.status === "not-found") {
    return (
      <AppShell>
        <div className="flex flex-col gap-6">
          <PageHeader eyebrow="Orders" title="Order Flow" description="The requested order could not be loaded." actions={headerActions} />
          <ErrorState title="Order not found" message={order.error} />
        </div>
      </AppShell>
    )
  }

  if (order.status === "error") {
    return (
      <AppShell>
        <div className="flex flex-col gap-6">
          <PageHeader eyebrow="Orders" title="Order Flow" description="The requested order could not be loaded." actions={headerActions} />
          <ErrorState title="Order unavailable" message={order.error} />
        </div>
      </AppShell>
    )
  }

  if (!order.data) {
    return (
      <AppShell>
        <div className="flex flex-col gap-6">
          <PageHeader eyebrow="Orders" title="Order Flow" description="No order data is available." actions={headerActions} />
          <EmptyState title="Order flow unavailable." description="No order data was returned for this route." />
        </div>
      </AppShell>
    )
  }

  return (
    <AppShell>
      <div className="flex flex-col gap-6">
        <PageHeader
          eyebrow="Orders"
          title="Order Flow"
          description={`Track ${order.data.reference} from reservation through payment and final order resolution.`}
          badge={<Badge variant={isPolling ? "secondary" : "outline"}>{isPolling ? "Polling every 2s" : "Polling stopped"}</Badge>}
          actions={headerActions}
        />

        <section className="grid gap-6 xl:grid-cols-[1.45fr_0.85fr] xl:items-start">
          <div className="flex flex-col gap-6">
            <SagaTimeline order={order.data} payment={payment.data} />
            <OrderLinesCard order={order.data} state={orderLines} />
          </div>
          <div className="flex flex-col gap-6">
            <PaymentPanel
              order={order.data}
              payment={payment}
              canAdmin={hasRole("ROLE_ADMIN")}
              actionError={actionError}
              isConfirmingPayment={isConfirmingPayment}
              isFailingPayment={isFailingPayment}
              onConfirmPayment={confirmPayment}
              onFailPayment={failPayment}
            />
            <OrderSummaryCard order={order.data} />
          </div>
        </section>
      </div>
    </AppShell>
  )
}
