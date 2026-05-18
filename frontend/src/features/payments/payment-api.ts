import { apiFetch } from "@/lib/api-client"
import type { PaymentMethod } from "@/features/orders/order-api"
import type { PaymentStatus } from "@/shared/status-meta"

export type PaymentResponse = {
  id: number
  paymentReference: string
  amount: number
  paymentMethod: PaymentMethod
  orderId: number
  status: PaymentStatus
  createdAt: string | null
  updatedAt: string | null
}

export function listPayments() {
  return apiFetch<PaymentResponse[]>("/api/v1/payments")
}

export function getPaymentByOrder(orderId: number) {
  return apiFetch<PaymentResponse>(`/api/v1/payments/orders/${orderId}`)
}

export function confirmDemoPayment(orderId: number) {
  return apiFetch<PaymentResponse>(`/api/v1/payments/demo/orders/${orderId}/confirm`, { method: "POST" })
}

export function failDemoPayment(orderId: number) {
  return apiFetch<PaymentResponse>(`/api/v1/payments/demo/orders/${orderId}/fail`, { method: "POST" })
}
