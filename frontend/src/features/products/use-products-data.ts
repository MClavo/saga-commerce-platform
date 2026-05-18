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

export type ProductsState =
  | { status: "loading"; data: ProductResponse[]; error: null }
  | { status: "success"; data: ProductResponse[]; error: null }
  | { status: "error"; data: ProductResponse[]; error: string }

function loadingState(): ProductsState {
  return { status: "loading", data: [], error: null }
}

async function loadProducts(): Promise<ProductsState> {
  try {
    return {
      status: "success",
      data: await listProducts(),
      error: null,
    }
  } catch (caught) {
    return {
      status: "error",
      data: [],
      error: caught instanceof Error ? caught.message : "Product request failed",
    }
  }
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
