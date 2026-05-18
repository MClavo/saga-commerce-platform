import { DetailItem } from "@/components/shared/DetailItem"
import { ProductStockBadge } from "@/features/products/components/ProductStockBadge"
import type { ProductResponse } from "@/features/products/product-api"
import { formatMoney } from "@/shared/formatters"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"

type ProductDetailSheetProps = {
  product: ProductResponse | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function ProductDetailSheet({ product, open, onOpenChange }: ProductDetailSheetProps) {
  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="sm:max-w-xl">
        <SheetHeader>
          <SheetTitle>{product?.name ?? "Product detail"}</SheetTitle>
          <SheetDescription>Catalog state used by product reservation.</SheetDescription>
        </SheetHeader>
        {product ? (
          <div className="flex flex-col gap-5 px-4 pb-4">
            <div className="rounded-xl bg-muted p-4">
              <p className="text-sm leading-relaxed text-muted-foreground">{product.description}</p>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <DetailItem label="Price" value={formatMoney(product.price)} mono />
              <DetailItem label="Available stock" value={product.availableQuantity.toString()} mono />
              <DetailItem label="Category" value={product.categoryName} />
              <DetailItem label="Category ID" value={product.categoryId.toString()} mono />
            </div>

            <div className="flex flex-col gap-2 rounded-xl border p-4">
              <span className="text-sm font-medium">Stock status</span>
              <ProductStockBadge availableQuantity={product.availableQuantity} />
            </div>

            <div className="flex flex-col gap-2 rounded-xl border p-4">
              <span className="text-sm font-medium">Category description</span>
              <p className="text-sm text-muted-foreground">{product.categoryDescription}</p>
            </div>
          </div>
        ) : null}
      </SheetContent>
    </Sheet>
  )
}
