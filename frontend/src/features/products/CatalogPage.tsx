import { useMemo, useState } from "react"

import { RoleGuard } from "@/features/auth/components/RoleGuard"
import { AppShell } from "@/components/layout/AppShell"
import { MetricCard } from "@/components/shared/MetricCard"
import { PageHeader } from "@/components/shared/PageHeader"
import { Button } from "@/components/ui/button"
import { ProductDetailSheet } from "@/features/products/components/ProductDetailSheet"
import { ProductFilters, type StockFilter } from "@/features/products/components/ProductFilters"
import { ProductFormDialog } from "@/features/products/components/ProductFormDialog"
import { ProductTable } from "@/features/products/components/ProductTable"
import { StockAdjustmentDialog } from "@/features/products/components/StockAdjustmentDialog"
import type { CreateProductRequest, ProductResponse, UpdateProductRequest } from "@/features/products/product-api"
import { getStockStatus } from "@/features/products/stock-status"
import { useProductsData } from "@/features/products/use-products-data"

export function CatalogPage() {
  const { products, isRefreshing, refresh, createProduct, updateProduct, adjustStock } = useProductsData()
  const [search, setSearch] = useState("")
  const [category, setCategory] = useState("ALL")
  const [stock, setStock] = useState<StockFilter>("ALL")
  const [selectedProduct, setSelectedProduct] = useState<ProductResponse | null>(null)
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [editingProduct, setEditingProduct] = useState<ProductResponse | null>(null)
  const [adjustingProduct, setAdjustingProduct] = useState<ProductResponse | null>(null)

  const categories = useMemo(
    () => [...new Set(products.data.map((product) => product.categoryName))].sort((first, second) => first.localeCompare(second)),
    [products.data]
  )

  const filteredProducts = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase()

    return products.data.filter((product) => {
      const matchesSearch = normalizedSearch
        ? [product.name, product.description, product.categoryName].some((value) => value.toLowerCase().includes(normalizedSearch))
        : true
      const matchesCategory = category === "ALL" || product.categoryName === category
      const matchesStock = stock === "ALL" || getStockStatus(product.availableQuantity).status === stock

      return matchesSearch && matchesCategory && matchesStock
    })
  }, [category, products.data, search, stock])

  const metrics = useMemo(() => {
    const lowStock = products.data.filter((product) => getStockStatus(product.availableQuantity).status === "LOW_STOCK").length
    const outOfStock = products.data.filter((product) => getStockStatus(product.availableQuantity).status === "OUT_OF_STOCK").length

    return {
      total: products.data.length,
      lowStock,
      outOfStock,
      categories: categories.length,
    }
  }, [categories.length, products.data])

  function resetFilters() {
    setSearch("")
    setCategory("ALL")
    setStock("ALL")
  }

  return (
    <AppShell>
      <div className="flex flex-col gap-6">
        <PageHeader
          eyebrow="Catalog / Products"
          title="Inspect stock before saga reservation touches it."
          description="Product stock is operational state. This page keeps price, category, and available quantity visible for demo review."
          actions={
            <>
            <Button type="button" variant="outline" onClick={() => void refresh()} disabled={isRefreshing}>
              {isRefreshing ? "Refreshing" : "Refresh"}
            </Button>
            <RoleGuard
              roles={["ROLE_ADMIN"]}
              fallback={
                <Button type="button" disabled>
                  Create Product
                </Button>
              }
            >
              <Button type="button" onClick={() => setIsCreateOpen(true)}>
                Create Product
              </Button>
            </RoleGuard>
            </>
          }
        />

        <section className="grid gap-3 md:grid-cols-2 xl:grid-cols-[1.2fr_0.8fr_1fr_0.7fr]">
          <MetricCard label="Total products" value={metrics.total} />
          <MetricCard label="Low stock" value={metrics.lowStock} />
          <MetricCard label="Out of stock" value={metrics.outOfStock} />
          <MetricCard label="Categories" value={metrics.categories} />
        </section>

        <ProductFilters
          search={search}
          category={category}
          stock={stock}
          categories={categories}
          onSearchChange={setSearch}
          onCategoryChange={setCategory}
          onStockChange={setStock}
          onReset={resetFilters}
        />

        <RoleGuard
          roles={["ROLE_ADMIN"]}
          fallback={<ProductTable state={products} products={filteredProducts} isAdmin={false} onView={setSelectedProduct} onEdit={() => undefined} onAdjustStock={() => undefined} />}
        >
          <ProductTable
            state={products}
            products={filteredProducts}
            isAdmin
            onView={setSelectedProduct}
            onEdit={setEditingProduct}
            onAdjustStock={setAdjustingProduct}
          />
        </RoleGuard>
      </div>

      <ProductDetailSheet product={selectedProduct} open={Boolean(selectedProduct)} onOpenChange={(open) => !open && setSelectedProduct(null)} />
      <ProductFormDialog
        mode="create"
        open={isCreateOpen}
        onOpenChange={setIsCreateOpen}
        onSubmit={(request) => createProduct(request as CreateProductRequest)}
      />
      <ProductFormDialog
        mode="edit"
        product={editingProduct ?? undefined}
        open={Boolean(editingProduct)}
        onOpenChange={(open) => !open && setEditingProduct(null)}
        onSubmit={(request) => (editingProduct ? updateProduct(editingProduct.id, request as UpdateProductRequest) : Promise.resolve())}
      />
      <StockAdjustmentDialog
        product={adjustingProduct}
        open={Boolean(adjustingProduct)}
        onOpenChange={(open) => !open && setAdjustingProduct(null)}
        onSubmit={(quantityDelta) => (adjustingProduct ? adjustStock(adjustingProduct.id, { quantityDelta }) : Promise.resolve())}
      />
    </AppShell>
  )
}
