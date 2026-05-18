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

export type CreateProductRequest = {
  name: string
  description: string
  availableQuantity: number
  price: number
  categoryId: number
}

export type UpdateProductRequest = {
  name: string
  description: string
  price: number
  categoryId: number
}

export type StockAdjustmentRequest = {
  quantityDelta: number
}

export function listProducts() {
  return apiFetch<ProductResponse[]>("/api/v1/products")
}

export function getProduct(productId: number) {
  return apiFetch<ProductResponse>(`/api/v1/products/${productId}`)
}

export function createProduct(request: CreateProductRequest) {
  return apiFetch<number>("/api/v1/products", {
    method: "POST",
    body: JSON.stringify(request),
  })
}

export function updateProduct(productId: number, request: UpdateProductRequest) {
  return apiFetch<ProductResponse>(`/api/v1/products/${productId}`, {
    method: "PUT",
    body: JSON.stringify(request),
  })
}

export function adjustProductStock(productId: number, request: StockAdjustmentRequest) {
  return apiFetch<ProductResponse>(`/api/v1/products/${productId}/stock-adjustments`, {
    method: "POST",
    body: JSON.stringify(request),
  })
}
