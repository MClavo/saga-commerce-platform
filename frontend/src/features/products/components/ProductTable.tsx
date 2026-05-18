import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { ProductStockBadge } from "@/features/products/components/ProductStockBadge"
import type { ProductResponse } from "@/features/products/product-api"
import type { ProductsState } from "@/features/products/use-products-data"
import { formatMoney } from "@/shared/formatters"

type ProductTableProps = {
  state: ProductsState
  products: ProductResponse[]
  isAdmin: boolean
  onView: (product: ProductResponse) => void
  onEdit: (product: ProductResponse) => void
  onAdjustStock: (product: ProductResponse) => void
}

export function ProductTable({ state, products, isAdmin, onView, onEdit, onAdjustStock }: ProductTableProps) {
  return (
    <Card>
      <CardHeader>
        <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
          <div className="flex flex-col gap-1">
            <CardTitle>Products</CardTitle>
            <CardDescription>Stock-first catalog view for reservation readiness.</CardDescription>
          </div>
          {!isAdmin ? <Badge variant="outline">Admin required for catalog mutations</Badge> : null}
        </div>
      </CardHeader>
      <CardContent>
        {state.status === "loading" ? <ProductTableSkeleton /> : null}
        {state.status === "error" ? <p className="rounded-lg bg-destructive/10 p-4 text-sm text-destructive">{state.error}</p> : null}
        {state.status === "success" && products.length === 0 ? (
          <div className="rounded-xl bg-muted p-6">
            <p className="font-medium">No products match current filters.</p>
            <p className="mt-1 text-sm text-muted-foreground">Reset filters or create a product as Admin.</p>
          </div>
        ) : null}
        {state.status === "success" && products.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Product</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>Price</TableHead>
                <TableHead>Available stock</TableHead>
                <TableHead>Stock status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {products.map((product) => (
                <TableRow key={product.id}>
                  <TableCell>
                    <div className="flex max-w-sm flex-col gap-1">
                      <span className="font-medium">{product.name}</span>
                      <span className="truncate text-xs text-muted-foreground">{product.description}</span>
                    </div>
                  </TableCell>
                  <TableCell>{product.categoryName}</TableCell>
                  <TableCell className="font-mono">{formatMoney(product.price)}</TableCell>
                  <TableCell className="font-mono">{product.availableQuantity}</TableCell>
                  <TableCell>
                    <ProductStockBadge availableQuantity={product.availableQuantity} />
                  </TableCell>
                  <TableCell>
                    <div className="flex justify-end gap-2">
                      <Button type="button" size="sm" variant="outline" onClick={() => onView(product)}>
                        View
                      </Button>
                      <Button type="button" size="sm" variant="outline" disabled={!isAdmin} onClick={() => onEdit(product)}>
                        Edit
                      </Button>
                      <Button
                        type="button"
                        size="sm"
                        variant="outline"
                        disabled={!isAdmin}
                        onClick={() => onAdjustStock(product)}
                      >
                        Stock
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : null}
      </CardContent>
    </Card>
  )
}

function ProductTableSkeleton() {
  return (
    <div className="flex flex-col gap-2">
      {Array.from({ length: 7 }).map((_, row) => (
        <div key={row} className="grid gap-2 md:grid-cols-[1.5fr_0.8fr_0.6fr_0.6fr_0.8fr_1fr]">
          {Array.from({ length: 6 }).map((__, column) => (
            <Skeleton key={column} className="h-8" />
          ))}
        </div>
      ))}
    </div>
  )
}
