import type { CustomerAddressRequest, CustomerAddressResponse, CustomerResponse } from "@/features/customers/customer-api"
import { ApiClientError } from "@/lib/api-client"

export function formatCustomerName(customer: Pick<CustomerResponse, "firstname" | "lastname">) {
  return [customer.firstname, customer.lastname].map((part) => part.trim()).filter(Boolean).join(" ") || "Unnamed customer"
}

export function formatAddressSummary(address: CustomerAddressResponse | CustomerAddressRequest | null | undefined) {
  if (!address) {
    return "No address"
  }

  const streetLine = [address.street, address.houseNumber].map((part) => part?.trim()).filter(Boolean).join(" ")
  const parts = [streetLine, address.zipCode?.trim()].filter(Boolean)

  return parts.length > 0 ? parts.join(", ") : "No address"
}

export function hasAddress(address: CustomerAddressResponse | null) {
  return formatAddressSummary(address) !== "No address"
}

export function compareCustomers(first: CustomerResponse, second: CustomerResponse) {
  return (
    first.lastname.localeCompare(second.lastname) ||
    first.firstname.localeCompare(second.firstname) ||
    first.email.localeCompare(second.email)
  )
}

export function getCustomerSearchText(customer: CustomerResponse) {
  return [
    formatCustomerName(customer),
    customer.firstname,
    customer.lastname,
    customer.email,
    customer.id,
    formatAddressSummary(customer.address),
  ]
    .join(" ")
    .toLowerCase()
}

export function formatCustomerApiError(caught: unknown, fallback: string) {
  if (caught instanceof ApiClientError) {
    if (typeof caught.body === "string") {
      return caught.body
    }

    if (isValidationErrorBody(caught.body)) {
      return Object.entries(caught.body.errors)
        .map(([field, message]) => `${field}: ${message}`)
        .join("; ")
    }
  }

  return caught instanceof Error ? caught.message : fallback
}

function isValidationErrorBody(body: unknown): body is { errors: Record<string, string> } {
  return Boolean(
    body &&
      typeof body === "object" &&
      "errors" in body &&
      body.errors &&
      typeof body.errors === "object" &&
      Object.values(body.errors).every((value) => typeof value === "string")
  )
}
