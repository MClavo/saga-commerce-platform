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
import type { ResourceState } from "@/features/dashboard/use-dashboard-data"
import type { PaymentResponse } from "@/features/payments/payment-api"
import { formatCompactDateTime, formatMoney } from "@/shared/formatters"
import { formatRequiredRoles } from "@/shared/role-labels"
import { getPaymentStatusMeta } from "@/shared/status-meta"

type LatestPaymentsCardProps = {
  state: ResourceState<PaymentResponse>
  payments: PaymentResponse[]
  hasMore: boolean
}

export function LatestPaymentsCard({ state, payments, hasMore }: LatestPaymentsCardProps) {
  return (
    <Card>
      <CardHeader>
        <div>
          <CardTitle>Latest payments</CardTitle>
          <CardDescription>Payment state created asynchronously by the saga.</CardDescription>
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
        {state.status === "success" && payments.length === 0 ? <Empty label="No payments have been created yet." /> : null}
        {state.status === "success" && payments.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Reference</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Order</TableHead>
                <TableHead>Created</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {payments.map((payment) => (
                <TableRow key={payment.id}>
                  <TableCell className="font-medium">{payment.paymentReference}</TableCell>
                  <TableCell>
                    <StatusBadge meta={getPaymentStatusMeta(payment.status)} status={payment.status} />
                  </TableCell>
                  <TableCell className="font-mono">{formatMoney(payment.amount)}</TableCell>
                  <TableCell className="font-mono text-xs text-muted-foreground">#{payment.orderId}</TableCell>
                  <TableCell>{formatCompactDateTime(payment.createdAt)}</TableCell>
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
