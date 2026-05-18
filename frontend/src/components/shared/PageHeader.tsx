import type { ReactNode } from "react"

import { cn } from "@/lib/utils"

type PageHeaderProps = {
  eyebrow: ReactNode
  title: ReactNode
  description: ReactNode
  actions?: ReactNode
  badge?: ReactNode
  className?: string
}

export function PageHeader({ eyebrow, title, description, actions, badge, className }: PageHeaderProps) {
  return (
    <section className={cn("grid gap-4 lg:grid-cols-[1.35fr_0.65fr] lg:items-end", className)}>
      <div className="flex flex-col gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <p className="font-mono text-xs uppercase tracking-[0.18em] text-muted-foreground">{eyebrow}</p>
          {badge}
        </div>
        <div className="flex flex-col gap-2">
          <h1 className="max-w-3xl text-3xl font-semibold tracking-tight md:text-5xl">{title}</h1>
          <p className="max-w-2xl text-base leading-relaxed text-muted-foreground">{description}</p>
        </div>
      </div>
      {actions ? <div className="flex flex-wrap justify-start gap-2 lg:justify-end">{actions}</div> : null}
    </section>
  )
}
