import { useState, type FormEvent } from "react"

import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import type { ProductResponse } from "@/features/products/product-api"

type StockAdjustmentDialogProps = {
  product: ProductResponse | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (quantityDelta: number) => Promise<void>
}

export function StockAdjustmentDialog({ product, open, onOpenChange, onSubmit }: StockAdjustmentDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {open && product ? (
        <StockAdjustmentContent
          key={product.id}
          product={product}
          onCancel={() => onOpenChange(false)}
          onSubmit={async (quantityDelta) => {
            await onSubmit(quantityDelta)
            onOpenChange(false)
          }}
        />
      ) : null}
    </Dialog>
  )
}

function StockAdjustmentContent({
  product,
  onCancel,
  onSubmit,
}: {
  product: ProductResponse
  onCancel: () => void
  onSubmit: (quantityDelta: number) => Promise<void>
}) {
  const [quantityDelta, setQuantityDelta] = useState("0")
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    const parsedDelta = Number(quantityDelta)

    if (!Number.isInteger(parsedDelta) || parsedDelta === 0) {
      setError("Quantity delta must be a nonzero integer.")
      return
    }

    if (product.availableQuantity + parsedDelta < 0) {
      setError("Adjustment cannot make available stock negative.")
      return
    }

    setIsSubmitting(true)

    try {
      await onSubmit(parsedDelta)
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Stock adjustment failed")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Adjust stock</DialogTitle>
        <DialogDescription>{product.name}: current available stock is {product.availableQuantity}.</DialogDescription>
      </DialogHeader>

      <form className="flex flex-col gap-4" onSubmit={(event) => void handleSubmit(event)}>
        <label className="flex flex-col gap-2">
          <span className="text-sm font-medium">Quantity delta</span>
          <Input value={quantityDelta} inputMode="numeric" onChange={(event) => setQuantityDelta(event.target.value)} />
          <span className="text-xs text-muted-foreground">Positive adds stock. Negative removes available stock.</span>
        </label>

        {error ? <p className="rounded-lg bg-destructive/10 p-3 text-sm text-destructive">{error}</p> : null}

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Saving" : "Save adjustment"}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  )
}
