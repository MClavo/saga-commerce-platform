import { cn } from "@/lib/utils"

type DetailItemProps = {
  label: string
  value: string
  mono?: boolean
}

export function DetailItem({ label, value, mono = false }: DetailItemProps) {
  return (
    <div className="flex flex-col gap-1 rounded-xl border p-3">
      <span className="text-xs text-muted-foreground">{label}</span>
      <span className={cn("text-sm font-medium", mono && "font-mono")}>{value}</span>
    </div>
  )
}
