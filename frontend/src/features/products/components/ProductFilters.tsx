import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"

export type StockFilter = "ALL" | "IN_STOCK" | "LOW_STOCK" | "OUT_OF_STOCK"

type ProductFiltersProps = {
  search: string
  category: string
  stock: StockFilter
  categories: string[]
  onSearchChange: (value: string) => void
  onCategoryChange: (value: string) => void
  onStockChange: (value: StockFilter) => void
  onReset: () => void
}

const stockOptions: Array<{ value: StockFilter; label: string }> = [
  { value: "ALL", label: "All stock" },
  { value: "IN_STOCK", label: "In Stock" },
  { value: "LOW_STOCK", label: "Low Stock" },
  { value: "OUT_OF_STOCK", label: "Out of Stock" },
]

export function ProductFilters({
  search,
  category,
  stock,
  categories,
  onSearchChange,
  onCategoryChange,
  onStockChange,
  onReset,
}: ProductFiltersProps) {
  return (
    <div className="grid gap-3 rounded-xl border bg-card p-4 md:grid-cols-[1.5fr_0.9fr_0.9fr_auto] md:items-end">
      <label className="flex flex-col gap-2">
        <span className="text-sm font-medium">Search</span>
        <Input
          placeholder="Name, description, category"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
        />
      </label>

      <label className="flex flex-col gap-2">
        <span className="text-sm font-medium">Category</span>
        <select
          className="h-8 rounded-lg border border-input bg-background px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
          value={category}
          onChange={(event) => onCategoryChange(event.target.value)}
        >
          <option value="ALL">All categories</option>
          {categories.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label className="flex flex-col gap-2">
        <span className="text-sm font-medium">Stock</span>
        <select
          className="h-8 rounded-lg border border-input bg-background px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
          value={stock}
          onChange={(event) => onStockChange(event.target.value as StockFilter)}
        >
          {stockOptions.map((item) => (
            <option key={item.value} value={item.value}>
              {item.label}
            </option>
          ))}
        </select>
      </label>

      <Button type="button" variant="outline" onClick={onReset}>
        Reset
      </Button>
    </div>
  )
}
