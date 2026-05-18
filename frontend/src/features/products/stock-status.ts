import type { BadgeVariant } from "@/shared/status-meta"

export type StockStatus = "IN_STOCK" | "LOW_STOCK" | "OUT_OF_STOCK"

export type StockStatusMeta = {
  status: StockStatus
  label: string
  variant: BadgeVariant
}

export function getStockStatus(availableQuantity: number): StockStatusMeta {
  if (availableQuantity <= 0) {
    return {
      status: "OUT_OF_STOCK",
      label: "Out of Stock",
      variant: "destructive",
    }
  }

  if (availableQuantity <= 5) {
    return {
      status: "LOW_STOCK",
      label: "Low Stock",
      variant: "secondary",
    }
  }

  return {
    status: "IN_STOCK",
    label: "In Stock",
    variant: "default",
  }
}
