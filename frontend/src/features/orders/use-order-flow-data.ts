import { useCallback, useEffect, useMemo, useState } from "react"

import { getOrder, getOrderLines, type OrderLineResponse, type OrderResponse } from "@/features/orders/order-api"
import { confirmDemoPayment, failDemoPayment, getPaymentByOrder, type PaymentResponse } from "@/features/payments/payment-api"
import { formatOrderApiError, isNotFoundError, isTerminalOrderStatus, shouldFetchPayment } from "@/features/orders/order-utils"
import { loadingState, type ResourceState } from "@/shared/resource-state"

type DetailState<T> =
  | { status: "loading"; data: T | null; error: null }
  | { status: "success"; data: T; error: null }
  | { status: "not-found"; data: null; error: string }
  | { status: "error"; data: T | null; error: string }

export type PaymentFlowState =
  | { status: "idle"; data: null; error: null; message: string }
  | { status: "loading"; data: PaymentResponse | null; error: null; message: string | null }
  | { status: "success"; data: PaymentResponse; error: null; message: null }
  | { status: "missing"; data: null; error: null; message: string }
  | { status: "error"; data: PaymentResponse | null; error: string; message: null }

const loadingDetail: DetailState<OrderResponse> = { status: "loading", data: null, error: null }

function idlePaymentState(order: OrderResponse | null): PaymentFlowState {
  if (order?.status === "PRODUCT_RESERVATION_FAILED") {
    return {
      status: "idle",
      data: null,
      error: null,
      message: "No payment was requested because product reservation failed.",
    }
  }

  return {
    status: "idle",
    data: null,
    error: null,
    message: "Waiting for product reservation before payment is requested.",
  }
}

export function useOrderFlowData(orderId: number | null) {
  const [order, setOrder] = useState<DetailState<OrderResponse>>(loadingDetail)
  const [orderLines, setOrderLines] = useState<ResourceState<OrderLineResponse>>(loadingState)
  const [payment, setPayment] = useState<PaymentFlowState>(idlePaymentState(null))
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [isConfirmingPayment, setIsConfirmingPayment] = useState(false)
  const [isFailingPayment, setIsFailingPayment] = useState(false)

  const refresh = useCallback(
    async ({ quiet = false }: { quiet?: boolean } = {}) => {
      if (!orderId) {
        setOrder({ status: "not-found", data: null, error: "Order id is invalid." })
        setOrderLines({ status: "error", data: [], error: "Order id is invalid." })
        setPayment(idlePaymentState(null))
        return
      }

      if (!quiet) {
        setIsRefreshing(true)
      }

      try {
        const nextOrder = await getOrder(orderId)
        setOrder({ status: "success", data: nextOrder, error: null })

        try {
          setOrderLines({ status: "success", data: await getOrderLines(orderId), error: null })
        } catch (caught) {
          setOrderLines({ status: "error", data: [], error: formatOrderApiError(caught, "Order lines request failed") })
        }

        if (!shouldFetchPayment(nextOrder.status)) {
          setPayment(idlePaymentState(nextOrder))
          return
        }

        setPayment((current) => ({ status: "loading", data: current.data, error: null, message: null }))

        try {
          setPayment({ status: "success", data: await getPaymentByOrder(orderId), error: null, message: null })
        } catch (caught) {
          if (isNotFoundError(caught) && nextOrder.status === "AWAITING_PAYMENT") {
            setPayment({ status: "missing", data: null, error: null, message: "Payment not created yet." })
            return
          }

          if (isNotFoundError(caught)) {
            setPayment({ status: "error", data: null, error: "Payment record not found for terminal order.", message: null })
            return
          }

          setPayment({ status: "error", data: null, error: formatOrderApiError(caught, "Payment request failed"), message: null })
        }
      } catch (caught) {
        if (isNotFoundError(caught)) {
          setOrder({ status: "not-found", data: null, error: "Order not found." })
        } else {
          setOrder({ status: "error", data: null, error: formatOrderApiError(caught, "Order request failed") })
        }

        setOrderLines({ status: "error", data: [], error: "Order lines unavailable because order could not be loaded." })
        setPayment(idlePaymentState(null))
      } finally {
        if (!quiet) {
          setIsRefreshing(false)
        }
      }
    },
    [orderId]
  )

  useEffect(() => {
    let active = true

    async function loadInitialFlow() {
      if (active) {
        await refresh()
      }
    }

    void loadInitialFlow()

    return () => {
      active = false
    }
  }, [refresh])

  const shouldPollOrder = order.status === "success" && !isTerminalOrderStatus(order.data.status)
  const shouldPollPayment = order.status === "success" && order.data.status === "AWAITING_PAYMENT" && payment.data?.status !== "CONFIRMED" && payment.data?.status !== "FAILED"
  const isPolling = shouldPollOrder || shouldPollPayment

  useEffect(() => {
    if (!isPolling) {
      return undefined
    }

    const interval = window.setInterval(() => {
      void refresh({ quiet: true })
    }, 2000)

    return () => window.clearInterval(interval)
  }, [isPolling, refresh])

  const paymentData = payment.data

  const confirmPayment = useCallback(async () => {
    if (!orderId) {
      return
    }

    setActionError(null)
    setIsConfirmingPayment(true)

    try {
      setPayment({ status: "success", data: await confirmDemoPayment(orderId), error: null, message: null })
      await refresh({ quiet: true })
    } catch (caught) {
      setActionError(formatOrderApiError(caught, "Payment confirmation failed"))
    } finally {
      setIsConfirmingPayment(false)
    }
  }, [orderId, refresh])

  const failPayment = useCallback(async () => {
    if (!orderId) {
      return
    }

    setActionError(null)
    setIsFailingPayment(true)

    try {
      setPayment({ status: "success", data: await failDemoPayment(orderId), error: null, message: null })
      await refresh({ quiet: true })
    } catch (caught) {
      setActionError(formatOrderApiError(caught, "Payment failure action failed"))
    } finally {
      setIsFailingPayment(false)
    }
  }, [orderId, refresh])

  const hasResolvedPayment = useMemo(() => paymentData?.status === "CONFIRMED" || paymentData?.status === "FAILED", [paymentData?.status])

  return {
    order,
    orderLines,
    payment,
    isRefreshing,
    isPolling,
    actionError,
    hasResolvedPayment,
    refresh,
    confirmPayment,
    failPayment,
    isConfirmingPayment,
    isFailingPayment,
  }
}
