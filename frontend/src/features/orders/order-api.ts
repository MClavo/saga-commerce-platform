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

export function listOrders() {
  return apiFetch<OrderResponse[]>("/api/v1/orders")
}
