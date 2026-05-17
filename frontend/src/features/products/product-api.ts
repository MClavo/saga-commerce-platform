import { apiFetch } from "@/lib/api-client"

export type ProductResponse = {
  id: number
  name: string
  description: string
  availableQuantity: number
  price: number
  categoryId: number
  categoryName: string
  categoryDescription: string
}

export function listProducts() {
  return apiFetch<ProductResponse[]>("/api/v1/products")
}
