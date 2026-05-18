import { useCallback, useEffect, useState } from "react"

import { listOrders, type OrderResponse } from "@/features/orders/order-api"
import { formatOrderApiError } from "@/features/orders/order-utils"
import { loadingState, type ResourceState } from "@/shared/resource-state"

export type OrdersState = ResourceState<OrderResponse>

async function loadOrders(): Promise<OrdersState> {
  try {
    return { status: "success", data: await listOrders(), error: null }
  } catch (caught) {
    return { status: "error", data: [], error: formatOrderApiError(caught, "Order request failed") }
  }
}

export function useOrdersData() {
  const [orders, setOrders] = useState<OrdersState>(loadingState)
  const [isRefreshing, setIsRefreshing] = useState(false)

  const refresh = useCallback(async () => {
    setIsRefreshing(true)
    setOrders(loadingState)
    setOrders(await loadOrders())
    setIsRefreshing(false)
  }, [])

  useEffect(() => {
    let active = true

    async function loadInitialOrders() {
      const nextOrders = await loadOrders()

      if (active) {
        setOrders(nextOrders)
      }
    }

    void loadInitialOrders()

    return () => {
      active = false
    }
  }, [])

  return {
    orders,
    isRefreshing,
    refresh,
  }
}
