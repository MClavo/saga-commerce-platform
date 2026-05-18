import type { PaymentMethod, OrderLineResponse, OrderResponse } from "@/features/orders/order-api"
import { ApiClientError } from "@/lib/api-client"
import type { OrderStatus } from "@/shared/status-meta"

export const orderReadRoles = ["ROLE_ORDER_MANAGER", "ROLE_ADMIN"]
export const terminalOrderStatuses: OrderStatus[] = ["CONFIRMED", "PRODUCT_RESERVATION_FAILED", "PAYMENT_FAILED"]
export const failedOrderStatuses: OrderStatus[] = ["PRODUCT_RESERVATION_FAILED", "PAYMENT_FAILED"]

export const orderStatusOptions: Array<{ value: OrderStatus | "ALL"; label: string }> = [
  { value: "ALL", label: "All statuses" },
  { value: "PRODUCT_RESERVATION_PENDING", label: "Reservation pending" },
  { value: "AWAITING_PAYMENT", label: "Awaiting payment" },
  { value: "CONFIRMED", label: "Confirmed" },
  { value: "PRODUCT_RESERVATION_FAILED", label: "Reservation failed" },
  { value: "PAYMENT_FAILED", label: "Payment failed" },
]

const paymentMethodLabels: Record<PaymentMethod, string> = {
  CREDIT_CARD: "Credit card",
  DEBIT_CARD: "Debit card",
  PAYPAL: "PayPal",
  VISA: "Visa",
  MASTERCARD: "Mastercard",
}

export function isTerminalOrderStatus(status: OrderStatus) {
  return terminalOrderStatuses.includes(status)
}

export function shouldFetchPayment(status: OrderStatus) {
  return status === "AWAITING_PAYMENT" || status === "CONFIRMED" || status === "PAYMENT_FAILED"
}

export function formatPaymentMethod(method: PaymentMethod) {
  return paymentMethodLabels[method] ?? method
}

export function isPendingOrderAmount(order: Pick<OrderResponse, "amount" | "status">) {
  return order.status === "PRODUCT_RESERVATION_PENDING" && roundMoneyToCents(order.amount) === 0
}

export function formatOrderAmount(order: Pick<OrderResponse, "amount" | "status">, pendingLabel = "not set") {
  return isPendingOrderAmount(order) ? pendingLabel : undefined
}

export function getOrderSearchText(order: OrderResponse) {
  return [order.reference, order.id, order.customerId].join(" ").toLowerCase()
}

export function getOrderLineTotal(line: OrderLineResponse) {
  return typeof line.unitPrice === "number" ? line.unitPrice * line.quantity : null
}

export function getConfirmedLinesTotal(lines: OrderLineResponse[]) {
  if (lines.length === 0 || lines.some((line) => typeof line.unitPrice !== "number")) {
    return null
  }

  return lines.reduce((total, line) => total + (getOrderLineTotal(line) ?? 0), 0)
}

export function roundMoneyToCents(value: number | null | undefined) {
  return typeof value === "number" && !Number.isNaN(value) ? Math.round(value * 100) : null
}

export function amountsDiffer(first: number | null | undefined, second: number | null | undefined) {
  const firstCents = roundMoneyToCents(first)
  const secondCents = roundMoneyToCents(second)

  return firstCents !== null && secondCents !== null && firstCents !== secondCents
}

export function isNotFoundError(caught: unknown) {
  return caught instanceof ApiClientError && caught.status === 404
}

export function formatOrderApiError(caught: unknown, fallback: string) {
  if (caught instanceof ApiClientError) {
    if (typeof caught.body === "string") {
      return caught.body
    }

    if (isValidationErrorBody(caught.body)) {
      return Object.entries(caught.body.errors)
        .map(([field, message]) => `${field}: ${message}`)
        .join("; ")
    }

    if (isMessageErrorBody(caught.body)) {
      return caught.body.message
    }
  }

  return caught instanceof Error ? caught.message : fallback
}

function isValidationErrorBody(body: unknown): body is { errors: Record<string, string> } {
  return Boolean(
    body &&
      typeof body === "object" &&
      "errors" in body &&
      body.errors &&
      typeof body.errors === "object" &&
      Object.values(body.errors).every((value) => typeof value === "string")
  )
}

function isMessageErrorBody(body: unknown): body is { message: string } {
  return Boolean(body && typeof body === "object" && "message" in body && typeof body.message === "string")
}
