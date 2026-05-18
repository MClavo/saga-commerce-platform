import { useCallback, useEffect, useState } from "react"

import {
  createCustomer,
  deleteCustomer,
  listCustomers,
  updateCustomer,
  type CustomerRequest,
  type CustomerResponse,
} from "@/features/customers/customer-api"
import { formatCustomerApiError } from "@/features/customers/customer-utils"
import { loadingState, type ResourceState } from "@/shared/resource-state"

export type CustomersState = ResourceState<CustomerResponse>

async function loadCustomers(): Promise<CustomersState> {
  try {
    return { status: "success", data: await listCustomers(), error: null }
  } catch (caught) {
    return { status: "error", data: [], error: formatCustomerApiError(caught, "Customer request failed") }
  }
}

export function useCustomersData() {
  const [customers, setCustomers] = useState<CustomersState>(loadingState)
  const [isRefreshing, setIsRefreshing] = useState(false)

  const refresh = useCallback(async () => {
    setIsRefreshing(true)
    setCustomers(loadingState)
    setCustomers(await loadCustomers())
    setIsRefreshing(false)
  }, [])

  useEffect(() => {
    let active = true

    async function loadInitialCustomers() {
      const nextCustomers = await loadCustomers()

      if (active) {
        setCustomers(nextCustomers)
      }
    }

    void loadInitialCustomers()

    return () => {
      active = false
    }
  }, [])

  const submitCreateCustomer = useCallback(
    async (request: CustomerRequest) => {
      await createCustomer(request)
      await refresh()
    },
    [refresh]
  )

  const submitUpdateCustomer = useCallback(
    async (customerId: string, request: CustomerRequest) => {
      await updateCustomer(customerId, request)
      await refresh()
    },
    [refresh]
  )

  const submitDeleteCustomer = useCallback(
    async (customerId: string) => {
      await deleteCustomer(customerId)
      await refresh()
    },
    [refresh]
  )

  return {
    customers,
    isRefreshing,
    refresh,
    createCustomer: submitCreateCustomer,
    updateCustomer: submitUpdateCustomer,
    deleteCustomer: submitDeleteCustomer,
  }
}
