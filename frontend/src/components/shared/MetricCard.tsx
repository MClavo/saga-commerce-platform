import { Badge } from "@/components/ui/badge"
import { Card, CardContent } from "@/components/ui/card"

type MetricCardProps = {
  label: string
  value: number | string
}

export function MetricCard({ label, value }: MetricCardProps) {
  return (
    <Card size="sm">
      <CardContent className="flex items-center justify-between gap-3">
        <span className="text-sm text-muted-foreground">{label}</span>
        <Badge variant="secondary" className="font-mono">
          {value}
        </Badge>
      </CardContent>
    </Card>
  )
}
