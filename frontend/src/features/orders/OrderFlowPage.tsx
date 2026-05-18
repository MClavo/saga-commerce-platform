import { Link, useParams } from "react-router-dom"

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
import { useOrderFlowData } from "@/features/orders/use-order-flow-data"

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
            <OrderSummaryCard order={order.data} />
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
          </div>
        </section>
      </div>
    </AppShell>
  )
}
