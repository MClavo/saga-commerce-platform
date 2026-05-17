import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import type { ResourceState } from "@/features/dashboard/use-dashboard-data"
import { formatRequiredRoles } from "@/shared/role-labels"

type SummaryCardProps<T> = {
  title: string
  description: string
  state: ResourceState<T>
  count: number | null
  detail: string
}

export function SummaryCard<T>({ title, description, state, count, detail }: SummaryCardProps<T>) {
  return (
    <Card size="sm">
      <CardHeader>
        <div className="flex items-start justify-between gap-3">
          <div className="flex flex-col gap-1">
            <CardTitle>{title}</CardTitle>
            <p className="text-xs text-muted-foreground">{description}</p>
          </div>
          {state.status === "restricted" ? <Badge variant="outline">Restricted</Badge> : null}
        </div>
      </CardHeader>
      <CardContent className="flex flex-col gap-3">
        {state.status === "loading" ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-8 w-20" />
            <Skeleton className="h-4 w-32" />
          </div>
        ) : null}

        {state.status === "success" ? (
          <div className="flex flex-col gap-1">
            <span className="font-mono text-3xl font-semibold tracking-tight">{count}</span>
            <span className="text-sm text-muted-foreground">{detail}</span>
          </div>
        ) : null}

        {state.status === "restricted" ? (
          <p className="text-sm text-muted-foreground">Restricted: requires {formatRequiredRoles(state.roles)}.</p>
        ) : null}

        {state.status === "error" ? <p className="text-sm text-destructive">{state.error}</p> : null}
      </CardContent>
    </Card>
  )
}
