import type { ReactNode } from "react"
import { Link } from "react-router-dom"

import { UserMenu } from "@/components/auth/UserMenu"
import { Badge } from "@/components/ui/badge"
import { LocalDevToolsMenu } from "@/components/layout/LocalDevToolsMenu"

const roadmapItems = ["Catalog", "Customers", "Orders", "Saga Demo"]

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-[100dvh] bg-background">
      <header className="sticky top-0 z-20 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80">
        <div className="mx-auto flex max-w-7xl flex-col gap-3 px-4 py-3 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex flex-col gap-3 md:flex-row md:items-center">
            <Link className="flex items-center gap-3" to="/dashboard">
              <span className="flex size-9 items-center justify-center rounded-lg bg-primary font-mono text-xs font-semibold text-primary-foreground">
                EC
              </span>
              <span className="flex flex-col leading-tight">
                <span className="font-semibold tracking-tight">E-Commerce Console</span>
                <span className="text-xs text-muted-foreground">Operations demo</span>
              </span>
            </Link>

            <nav aria-label="Primary navigation" className="flex flex-wrap items-center gap-2 md:border-l md:pl-3">
              <Badge>Dashboard</Badge>
              {roadmapItems.map((item) => (
                <Badge key={item} aria-disabled="true" variant="outline">
                  {item}
                </Badge>
              ))}
            </nav>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-end">
            <LocalDevToolsMenu />
            <UserMenu />
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-6 md:py-8">{children}</main>
    </div>
  )
}
