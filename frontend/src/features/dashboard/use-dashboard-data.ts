import { useCallback, useEffect, useState } from "react"

import { listCustomers, type CustomerResponse } from "@/features/customers/customer-api"
import { listOrders, type OrderResponse } from "@/features/orders/order-api"
import { listPayments, type PaymentResponse } from "@/features/payments/payment-api"
import { listProducts, type ProductResponse } from "@/features/products/product-api"
import { useAuth } from "@/features/auth/use-auth"
import { compareByNewestDateThenId } from "@/shared/formatters"
import { activeOrderStatuses } from "@/shared/status-meta"
import type { Authority } from "@/shared/auth-types"

export const customerReadRoles: Authority[] = ["ROLE_CUSTOMER_SUPPORT", "ROLE_ADMIN"]
export const orderReadRoles: Authority[] = ["ROLE_ORDER_MANAGER", "ROLE_ADMIN"]
export const paymentReadRoles: Authority[] = ["ROLE_ORDER_MANAGER", "ROLE_ADMIN"]

export type ResourceState<T> =
  | { status: "loading"; data: T[]; error: null }
  | { status: "success"; data: T[]; error: null }
  | { status: "error"; data: T[]; error: string }
  | { status: "restricted"; data: T[]; error: null; roles: Authority[] }

async function loadResource<T>(loader: () => Promise<T[]>): Promise<ResourceState<T>> {
  try {
    return {
      status: "success",
      data: await loader(),
      error: null,
    }
  } catch (caught) {
    return {
      status: "error",
      data: [],
      error: caught instanceof Error ? caught.message : "Request failed",
    }
  }
}

function loadingState<T>(): ResourceState<T> {
  return { status: "loading", data: [], error: null }
}

function restrictedState<T>(roles: Authority[]): ResourceState<T> {
  return { status: "restricted", data: [], error: null, roles }
}

async function loadDashboardResources(
  canReadCustomers: boolean,
  canReadOrders: boolean,
  canReadPayments: boolean
) {
  const [nextProducts, nextCustomers, nextOrders, nextPayments] = await Promise.all([
    loadResource(listProducts),
    canReadCustomers
      ? loadResource(listCustomers)
      : Promise.resolve(restrictedState<CustomerResponse>(customerReadRoles)),
    canReadOrders
      ? loadResource(listOrders)
      : Promise.resolve(restrictedState<OrderResponse>(orderReadRoles)),
    canReadPayments
      ? loadResource(listPayments)
      : Promise.resolve(restrictedState<PaymentResponse>(paymentReadRoles)),
  ])

  return { nextProducts, nextCustomers, nextOrders, nextPayments }
}

export type DashboardData = {
  products: ResourceState<ProductResponse>
  customers: ResourceState<CustomerResponse>
  orders: ResourceState<OrderResponse>
  payments: ResourceState<PaymentResponse>
  latestOrders: OrderResponse[]
  latestPayments: PaymentResponse[]
  counts: {
    products: number | null
    customers: number | null
    orders: number | null
    payments: number | null
  }
  details: {
    lowStockProducts: number | null
    customersWithAddress: number | null
    activeOrders: number | null
    pendingPayments: number | null
  }
  hasMore: {
    orders: boolean
    payments: boolean
  }
  isRefreshing: boolean
  refresh: () => Promise<void>
}

export function useDashboardData(): DashboardData {
  const { hasAnyRole } = useAuth()
  const canReadCustomers = hasAnyRole(customerReadRoles)
  const canReadOrders = hasAnyRole(orderReadRoles)
  const canReadPayments = hasAnyRole(paymentReadRoles)
  const [products, setProducts] = useState<ResourceState<ProductResponse>>(loadingState)
  const [customers, setCustomers] = useState<ResourceState<CustomerResponse>>(
    canReadCustomers ? loadingState : () => restrictedState(customerReadRoles)
  )
  const [orders, setOrders] = useState<ResourceState<OrderResponse>>(
    canReadOrders ? loadingState : () => restrictedState(orderReadRoles)
  )
  const [payments, setPayments] = useState<ResourceState<PaymentResponse>>(
    canReadPayments ? loadingState : () => restrictedState(paymentReadRoles)
  )
  const [isRefreshing, setIsRefreshing] = useState(false)

  const refresh = useCallback(async () => {
    setIsRefreshing(true)
    setProducts(loadingState)
    setCustomers(canReadCustomers ? loadingState : restrictedState(customerReadRoles))
    setOrders(canReadOrders ? loadingState : restrictedState(orderReadRoles))
    setPayments(canReadPayments ? loadingState : restrictedState(paymentReadRoles))

    const { nextProducts, nextCustomers, nextOrders, nextPayments } = await loadDashboardResources(
      canReadCustomers,
      canReadOrders,
      canReadPayments
    )

    setProducts(nextProducts)
    setCustomers(nextCustomers)
    setOrders(nextOrders)
    setPayments(nextPayments)
    setIsRefreshing(false)
  }, [canReadCustomers, canReadOrders, canReadPayments])

  useEffect(() => {
    let active = true

    async function loadInitialData() {
      const { nextProducts, nextCustomers, nextOrders, nextPayments } = await loadDashboardResources(
        canReadCustomers,
        canReadOrders,
        canReadPayments
      )

      if (!active) {
        return
      }

      setProducts(nextProducts)
      setCustomers(nextCustomers)
      setOrders(nextOrders)
      setPayments(nextPayments)
    }

    void loadInitialData()

    return () => {
      active = false
    }
  }, [canReadCustomers, canReadOrders, canReadPayments])

  const latestOrders = [...orders.data].sort(compareByNewestDateThenId).slice(0, 5)
  const latestPayments = [...payments.data].sort(compareByNewestDateThenId).slice(0, 5)

  return {
    products,
    customers,
    orders,
    payments,
    latestOrders,
    latestPayments,
    counts: {
      products: products.status === "success" ? products.data.length : null,
      customers: customers.status === "success" ? customers.data.length : null,
      orders: orders.status === "success" ? orders.data.length : null,
      payments: payments.status === "success" ? payments.data.length : null,
    },
    details: {
      lowStockProducts:
        products.status === "success"
          ? products.data.filter((product) => product.availableQuantity > 0 && product.availableQuantity <= 5).length
          : null,
      customersWithAddress:
        customers.status === "success"
          ? customers.data.filter((customer) => Boolean(customer.address)).length
          : null,
      activeOrders:
        orders.status === "success"
          ? orders.data.filter((order) => activeOrderStatuses.includes(order.status)).length
          : null,
      pendingPayments:
        payments.status === "success"
          ? payments.data.filter((payment) => payment.status === "PENDING").length
          : null,
    },
    hasMore: {
      orders: orders.data.length > 5,
      payments: payments.data.length > 5,
    },
    isRefreshing,
    refresh,
  }
}
