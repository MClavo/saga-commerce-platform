import { Button } from "@/components/ui/button"
import { EmptyState, ErrorState, RestrictedState, TableSkeleton } from "@/components/shared/DataState"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { StatusBadge } from "@/components/shared/StatusBadge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import type { ResourceState } from "@/shared/resource-state"
import type { PaymentResponse } from "@/features/payments/payment-api"
import { formatCompactDateTime, formatMoney } from "@/shared/formatters"
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
        {state.status === "restricted" ? <RestrictedState roles={state.roles} /> : null}
        {state.status === "error" ? <ErrorState compact message={state.error} /> : null}
        {state.status === "success" && payments.length === 0 ? <EmptyState compact title="No payments have been created yet." /> : null}
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
