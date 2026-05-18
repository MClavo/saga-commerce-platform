import { Badge } from "@/components/ui/badge"
import { getStockStatus } from "@/features/products/stock-status"

export function ProductStockBadge({ availableQuantity }: { availableQuantity: number }) {
  const meta = getStockStatus(availableQuantity)

  return (
    <div className="flex flex-col gap-1">
      <Badge className="w-fit" variant={meta.variant}>
        {meta.label}
      </Badge>
      <span className="font-mono text-xs text-muted-foreground">{meta.status}</span>
    </div>
  )
}
