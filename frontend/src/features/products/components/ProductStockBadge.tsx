import { StatusBadge } from "@/components/shared/StatusBadge"
import { getStockStatus } from "@/features/products/stock-status"

export function ProductStockBadge({ availableQuantity }: { availableQuantity: number }) {
  const meta = getStockStatus(availableQuantity)

  return <StatusBadge meta={meta} status={meta.status} />
}
