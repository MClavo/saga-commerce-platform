import { useMemo, useState } from "react"

import { AppShell } from "@/components/layout/AppShell"
import { MetricCard } from "@/components/shared/MetricCard"
import { PageHeader } from "@/components/shared/PageHeader"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { OrderFilters, type OrderStatusFilter } from "@/features/orders/components/OrderFilters"
import { OrderTable } from "@/features/orders/components/OrderTable"
import { failedOrderStatuses, getOrderSearchText } from "@/features/orders/order-utils"
import { useOrdersData } from "@/features/orders/use-orders-data"
import { activeOrderStatuses } from "@/shared/status-meta"
import { compareByNewestDateThenId } from "@/shared/formatters"

export function OrdersPage() {
  const { orders, isRefreshing, refresh } = useOrdersData()
  const [search, setSearch] = useState("")
  const [status, setStatus] = useState<OrderStatusFilter>("ALL")

  const visibleOrders = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase()
    const sortedOrders = [...orders.data].sort(compareByNewestDateThenId)

    return sortedOrders.filter((order) => {
      const matchesSearch = normalizedSearch ? getOrderSearchText(order).includes(normalizedSearch) : true
      const matchesStatus = status === "ALL" || order.status === status

      return matchesSearch && matchesStatus
    })
  }, [orders.data, search, status])

  const metrics = useMemo(
    () => ({
      total: orders.data.length,
      active: orders.data.filter((order) => activeOrderStatuses.includes(order.status)).length,
      awaitingPayment: orders.data.filter((order) => order.status === "AWAITING_PAYMENT").length,
      failed: orders.data.filter((order) => failedOrderStatuses.includes(order.status)).length,
    }),
    [orders.data]
  )

  function resetFilters() {
    setSearch("")
    setStatus("ALL")
  }

  return (
    <AppShell>
      <div className="flex flex-col gap-6">
        <PageHeader
          eyebrow="Orders"
          title="Inspect order lifecycle and saga state."
          description="Order records expose customer purchase requests, reservation progress, payment state, and terminal outcomes."
          badge={<Badge variant="outline">Restricted: Order Manager / Admin</Badge>}
          actions={
            <Button type="button" variant="outline" onClick={() => void refresh()} disabled={isRefreshing}>
              {isRefreshing ? "Refreshing" : "Refresh"}
            </Button>
          }
        />

        <section className="grid gap-3 md:grid-cols-2 xl:grid-cols-[1.1fr_1fr_1fr_0.9fr]">
          <MetricCard label="Total orders" value={metrics.total} />
          <MetricCard label="Active sagas" value={metrics.active} />
          <MetricCard label="Awaiting payment" value={metrics.awaitingPayment} />
          <MetricCard label="Failed orders" value={metrics.failed} />
        </section>

        <OrderFilters search={search} status={status} onSearchChange={setSearch} onStatusChange={setStatus} onReset={resetFilters} />
        <OrderTable state={orders} orders={visibleOrders} totalCount={orders.data.length} />
      </div>
    </AppShell>
  )
}
