import { useCallback, useEffect, useState } from "react"

import {
  adjustProductStock,
  createProduct,
  listProducts,
  updateProduct,
  type CreateProductRequest,
  type ProductResponse,
  type StockAdjustmentRequest,
  type UpdateProductRequest,
} from "@/features/products/product-api"
import { loadResource, loadingState, type ResourceState } from "@/shared/resource-state"

export type ProductsState = ResourceState<ProductResponse>

async function loadProducts(): Promise<ProductsState> {
  return loadResource(listProducts, "Product request failed")
}

export function useProductsData() {
  const [products, setProducts] = useState<ProductsState>(loadingState)
  const [isRefreshing, setIsRefreshing] = useState(false)

  const refresh = useCallback(async () => {
    setIsRefreshing(true)
    setProducts(loadingState)
    setProducts(await loadProducts())
    setIsRefreshing(false)
  }, [])

  useEffect(() => {
    let active = true

    async function loadInitialProducts() {
      const nextProducts = await loadProducts()

      if (active) {
        setProducts(nextProducts)
      }
    }

    void loadInitialProducts()

    return () => {
      active = false
    }
  }, [])

  const submitCreateProduct = useCallback(
    async (request: CreateProductRequest) => {
      await createProduct(request)
      await refresh()
    },
    [refresh]
  )

  const submitUpdateProduct = useCallback(
    async (productId: number, request: UpdateProductRequest) => {
      await updateProduct(productId, request)
      await refresh()
    },
    [refresh]
  )

  const submitStockAdjustment = useCallback(
    async (productId: number, request: StockAdjustmentRequest) => {
      await adjustProductStock(productId, request)
      await refresh()
    },
    [refresh]
  )

  return {
    products,
    isRefreshing,
    refresh,
    createProduct: submitCreateProduct,
    updateProduct: submitUpdateProduct,
    adjustStock: submitStockAdjustment,
  }
}
