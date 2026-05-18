import { AppShell } from "@/components/layout/AppShell"
import { Button } from "@/components/ui/button"
import { SummaryCard } from "@/features/dashboard/components/SummaryCard"
import { LatestOrdersCard } from "@/features/dashboard/components/LatestOrdersCard"
import { LatestPaymentsCard } from "@/features/dashboard/components/LatestPaymentsCard"
import { SagaFlowCard } from "@/features/dashboard/components/SagaFlowCard"
import { useDashboardData } from "@/features/dashboard/use-dashboard-data"

export function DashboardPage() {
  const dashboard = useDashboardData()

  return (
    <AppShell>
      <div className="flex flex-col gap-6">
        <section className="grid gap-4 lg:grid-cols-[1.45fr_0.55fr] lg:items-end">
          <div className="flex flex-col gap-3">
            <p className="font-mono text-xs uppercase tracking-[0.18em] text-muted-foreground">Technical demo console</p>
            <div className="flex flex-col gap-2">
              <h1 className="max-w-3xl text-3xl font-semibold tracking-tight md:text-5xl">
                Operational view of the Ecommerce backend.
              </h1>
              <p className="max-w-2xl text-base leading-relaxed text-muted-foreground">
                Real counts, role-aware sections, latest saga activity, and local infrastructure links for reviewers.
              </p>
            </div>
          </div>
          <div className="flex justify-start lg:justify-end">
            <Button type="button" variant="outline" onClick={() => void dashboard.refresh()} disabled={dashboard.isRefreshing}>
              {dashboard.isRefreshing ? "Refreshing" : "Refresh data"}
            </Button>
          </div>
        </section>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-[1.2fr_0.9fr_1.1fr_0.8fr]">
          <SummaryCard
            count={dashboard.counts.products}
            description="Catalog inventory records"
            detail={`Low stock: ${dashboard.details.lowStockProducts ?? 0}`}
            state={dashboard.products}
            title="Products"
          />
          <SummaryCard
            count={dashboard.counts.customers}
            description="Customer records"
            detail={`With address: ${dashboard.details.customersWithAddress ?? 0}`}
            state={dashboard.customers}
            title="Customers"
          />
          <SummaryCard
            count={dashboard.counts.orders}
            description="Order saga records"
            detail={`Active: ${dashboard.details.activeOrders ?? 0}`}
            state={dashboard.orders}
            title="Orders"
          />
          <SummaryCard
            count={dashboard.counts.payments}
            description="Payment records"
            detail={`Pending: ${dashboard.details.pendingPayments ?? 0}`}
            state={dashboard.payments}
            title="Payments"
          />
        </section>

        <SagaFlowCard />

        <section className="grid gap-4 xl:grid-cols-[1.05fr_0.95fr]">
          <LatestOrdersCard hasMore={dashboard.hasMore.orders} orders={dashboard.latestOrders} state={dashboard.orders} />
          <LatestPaymentsCard hasMore={dashboard.hasMore.payments} payments={dashboard.latestPayments} state={dashboard.payments} />
        </section>
      </div>
    </AppShell>
  )
}
