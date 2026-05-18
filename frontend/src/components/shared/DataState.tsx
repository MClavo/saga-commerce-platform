import type { ReactNode } from "react"

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Empty, EmptyDescription, EmptyHeader, EmptyTitle } from "@/components/ui/empty"
import { Skeleton } from "@/components/ui/skeleton"
import { formatRequiredRoles } from "@/shared/role-labels"

type TableSkeletonProps = {
  columns: number
  rows?: number
  className?: string
}

export function TableSkeleton({ columns, rows = 5, className }: TableSkeletonProps) {
  return (
    <div className="flex flex-col gap-2">
      {Array.from({ length: rows }).map((_, row) => (
        <div key={row} className={className ?? "grid gap-2"} style={className ? undefined : { gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}>
          {Array.from({ length: columns }).map((__, column) => (
            <Skeleton key={column} className="h-8" />
          ))}
        </div>
      ))}
    </div>
  )
}

export function RestrictedState({ roles }: { roles: string[] }) {
  return <p className="text-sm text-muted-foreground">Restricted: requires {formatRequiredRoles(roles)}.</p>
}

export function ErrorState({ title, message, compact = false }: { title?: string; message: string; compact?: boolean }) {
  if (compact) {
    return <p className="text-sm text-destructive">{message}</p>
  }

  return (
    <Alert variant="destructive">
      {title ? <AlertTitle>{title}</AlertTitle> : null}
      <AlertDescription>{message}</AlertDescription>
    </Alert>
  )
}

export function EmptyState({ title, description, compact = false }: { title: ReactNode; description?: ReactNode; compact?: boolean }) {
  if (compact) {
    return <p className="rounded-lg bg-muted p-4 text-sm text-muted-foreground">{title}</p>
  }

  return (
    <Empty>
      <EmptyHeader>
        <EmptyTitle>{title}</EmptyTitle>
        {description ? <EmptyDescription>{description}</EmptyDescription> : null}
      </EmptyHeader>
    </Empty>
  )
}
