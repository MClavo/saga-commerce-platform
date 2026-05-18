import { apiFetch } from "@/lib/api-client"
import type { OrderStatus } from "@/shared/status-meta"

export type PaymentMethod = "CREDIT_CARD" | "DEBIT_CARD" | "PAYPAL" | "VISA" | "MASTERCARD"

export type OrderResponse = {
  id: number
  reference: string
  amount: number
  paymentMethod: PaymentMethod
  customerId: string
  status: OrderStatus
  createdAt: string | null
}

export type OrderLineResponse = {
  id: number
  productId: number
  productName: string | null
  quantity: number
  unitPrice: number | null
}

export function listOrders() {
  return apiFetch<OrderResponse[]>("/api/v1/orders")
}

export function getOrder(orderId: number) {
  return apiFetch<OrderResponse>(`/api/v1/orders/${orderId}`)
}

export function getOrderLines(orderId: number) {
  return apiFetch<OrderLineResponse[]>(`/api/v1/orders/${orderId}/order-lines`)
}
