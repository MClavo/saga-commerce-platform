import type { VariantProps } from "class-variance-authority"

import { badgeVariants } from "@/components/ui/badge"

export type BadgeVariant = NonNullable<VariantProps<typeof badgeVariants>["variant"]>

export type StatusMeta = {
  label: string
  variant: BadgeVariant
}

export type OrderStatus =
  | "PRODUCT_RESERVATION_PENDING"
  | "AWAITING_PAYMENT"
  | "CONFIRMED"
  | "PRODUCT_RESERVATION_FAILED"
  | "PAYMENT_FAILED"

export type PaymentStatus = "PENDING" | "CONFIRMED" | "FAILED"

const orderStatusMeta: Record<OrderStatus, StatusMeta> = {
  PRODUCT_RESERVATION_PENDING: {
    label: "Reservation pending",
    variant: "secondary",
  },
  AWAITING_PAYMENT: {
    label: "Awaiting payment",
    variant: "secondary",
  },
  CONFIRMED: {
    label: "Confirmed",
    variant: "default",
  },
  PRODUCT_RESERVATION_FAILED: {
    label: "Reservation failed",
    variant: "destructive",
  },
  PAYMENT_FAILED: {
    label: "Payment failed",
    variant: "destructive",
  },
}

const paymentStatusMeta: Record<PaymentStatus, StatusMeta> = {
  PENDING: {
    label: "Pending",
    variant: "secondary",
  },
  CONFIRMED: {
    label: "Confirmed",
    variant: "default",
  },
  FAILED: {
    label: "Failed",
    variant: "destructive",
  },
}

export function getOrderStatusMeta(status: OrderStatus): StatusMeta {
  return orderStatusMeta[status] ?? { label: status, variant: "outline" }
}

export function getPaymentStatusMeta(status: PaymentStatus): StatusMeta {
  return paymentStatusMeta[status] ?? { label: status, variant: "outline" }
}

export const activeOrderStatuses: OrderStatus[] = [
  "PRODUCT_RESERVATION_PENDING",
  "AWAITING_PAYMENT",
]
