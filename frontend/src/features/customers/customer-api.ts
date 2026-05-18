import { apiFetch } from "@/lib/api-client"

export type CustomerAddressResponse = {
  street?: string | null
  houseNumber?: string | null
  zipCode?: string | null
}

export type CustomerAddressRequest = {
  street?: string
  houseNumber?: string
  zipCode?: string
}

export type CustomerRequest = {
  firstname: string
  lastname: string
  email: string
  address: CustomerAddressRequest | null
}

export type CustomerResponse = {
  id: string
  firstname: string
  lastname: string
  email: string
  address: CustomerAddressResponse | null
}

export function listCustomers() {
  return apiFetch<CustomerResponse[]>("/api/v1/customers")
}

export function createCustomer(request: CustomerRequest) {
  return apiFetch<CustomerResponse>("/api/v1/customers", {
    method: "POST",
    body: JSON.stringify(request),
  })
}

export function updateCustomer(customerId: string, request: CustomerRequest) {
  return apiFetch<void>(`/api/v1/customers/${customerId}`, {
    method: "PUT",
    body: JSON.stringify(request),
  })
}

export function deleteCustomer(customerId: string) {
  return apiFetch<void>(`/api/v1/customers/${customerId}`, {
    method: "DELETE",
  })
}
