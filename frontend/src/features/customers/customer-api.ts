import { apiFetch } from "@/lib/api-client"

export type CustomerAddressResponse = {
  id?: string
  street?: string
  houseNumber?: string
  zipCode?: string
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
