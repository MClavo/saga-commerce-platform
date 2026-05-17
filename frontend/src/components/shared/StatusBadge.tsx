import { Badge } from "@/components/ui/badge"
import type { StatusMeta } from "@/shared/status-meta"

type StatusBadgeProps = {
  status: string
  meta: StatusMeta
}

export function StatusBadge({ status, meta }: StatusBadgeProps) {
  return (
    <div className="flex flex-col gap-1">
      <Badge className="w-fit" variant={meta.variant}>
        {meta.label}
      </Badge>
      <span className="font-mono text-xs text-muted-foreground">{status}</span>
    </div>
  )
}
