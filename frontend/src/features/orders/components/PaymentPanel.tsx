import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { DetailItem } from "@/components/shared/DetailItem"
import { StatusBadge } from "@/components/shared/StatusBadge"
import type { OrderResponse } from "@/features/orders/order-api"
import { amountsDiffer, formatPaymentMethod, isPendingOrderAmount } from "@/features/orders/order-utils"
import type { PaymentFlowState } from "@/features/orders/use-order-flow-data"
import { formatCompactDateTime, formatMoney } from "@/shared/formatters"
import { getPaymentStatusMeta } from "@/shared/status-meta"

type PaymentPanelProps = {
  order: OrderResponse
  payment: PaymentFlowState
  canAdmin: boolean
  actionError: string | null
  isConfirmingPayment: boolean
  isFailingPayment: boolean
  onConfirmPayment: () => Promise<void>
  onFailPayment: () => Promise<void>
}

export function PaymentPanel({
  order,
  payment,
  canAdmin,
  actionError,
  isConfirmingPayment,
  isFailingPayment,
  onConfirmPayment,
  onFailPayment,
}: PaymentPanelProps) {
  const paymentData = payment.data
  const canActOnPayment = order.status === "AWAITING_PAYMENT" && paymentData?.status === "PENDING"
  const hasAmountMismatch = paymentData ? !isPendingOrderAmount(order) && amountsDiffer(paymentData.amount, order.amount) : false

  return (
    <Card>
      <CardHeader>
        <CardTitle>Payment state</CardTitle>
        <CardDescription>Payment inspection and demo-only payment resolution actions.</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {actionError ? (
          <Alert variant="destructive">
            <AlertTitle>Payment action failed</AlertTitle>
            <AlertDescription>{actionError}</AlertDescription>
          </Alert>
        ) : null}
        {hasAmountMismatch ? (
          <Alert>
            <AlertTitle>Payment amount differs from order amount</AlertTitle>
            <AlertDescription>Payment amount should match the amount provided by Order Service.</AlertDescription>
          </Alert>
        ) : null}
        {payment.status === "idle" || payment.status === "missing" ? (
          <p className="rounded-lg bg-muted p-4 text-sm text-muted-foreground">{payment.message}</p>
        ) : null}
        {payment.status === "loading" && !paymentData ? <p className="text-sm text-muted-foreground">Loading payment state.</p> : null}
        {payment.status === "error" ? (
          <Alert variant="destructive">
            <AlertTitle>Payment unavailable</AlertTitle>
            <AlertDescription>{payment.error}</AlertDescription>
          </Alert>
        ) : null}
        {paymentData ? (
          <div className="grid gap-3">
            <div className="rounded-xl border p-3">
              <span className="text-xs text-muted-foreground">Status</span>
              <div className="mt-2">
                <StatusBadge meta={getPaymentStatusMeta(paymentData.status)} status={paymentData.status} />
              </div>
            </div>
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
              <DetailItem label="Reference" value={paymentData.paymentReference} />
              <DetailItem label="Payment ID" value={`#${paymentData.id}`} mono />
              <DetailItem label="Amount" value={formatMoney(paymentData.amount)} mono />
              <DetailItem label="Payment method" value={formatPaymentMethod(paymentData.paymentMethod)} />
              <DetailItem label="Created" value={formatCompactDateTime(paymentData.createdAt)} />
              <DetailItem label="Updated" value={formatCompactDateTime(paymentData.updatedAt)} />
            </div>
          </div>
        ) : null}
      </CardContent>
      {canActOnPayment ? (
        <CardFooter className="flex flex-col items-stretch gap-2 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-sm text-muted-foreground">{canAdmin ? "Resolve this demo payment." : "Admin role required for demo payment actions."}</p>
          <div className="flex flex-col gap-2 sm:flex-row">
            <Button type="button" onClick={() => void onConfirmPayment()} disabled={!canAdmin || isConfirmingPayment || isFailingPayment}>
              {isConfirmingPayment ? "Confirming" : "Confirm Payment"}
            </Button>
            <AlertDialog>
              <AlertDialogTrigger asChild>
                <Button type="button" variant="destructive" disabled={!canAdmin || isConfirmingPayment || isFailingPayment}>
                  {isFailingPayment ? "Failing" : "Fail Payment"}
                </Button>
              </AlertDialogTrigger>
              <AlertDialogContent>
                <AlertDialogHeader>
                  <AlertDialogTitle>Fail this payment?</AlertDialogTitle>
                  <AlertDialogDescription>
                    This demo action publishes a payment failure and drives the order toward payment failure resolution.
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel>Cancel</AlertDialogCancel>
                  <AlertDialogAction variant="destructive" onClick={() => void onFailPayment()}>
                    Fail Payment
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          </div>
        </CardFooter>
      ) : null}
    </Card>
  )
}
