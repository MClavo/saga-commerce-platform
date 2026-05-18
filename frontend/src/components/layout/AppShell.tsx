import type { ReactNode } from "react"
import { Link, useLocation } from "react-router-dom"

import { UserMenu } from "@/features/auth/components/UserMenu"
import { LocalDevToolsMenu } from "@/components/layout/LocalDevToolsMenu"
import { cn } from "@/lib/utils"

const navItems = [
  { label: "Dashboard", href: "/dashboard" },
  { label: "Catalog", href: "/catalog" },
  { label: "Customers", href: "/customers" },
  { label: "Orders" },
  { label: "Saga Demo" },
]

export function AppShell({ children }: { children: ReactNode }) {
  const location = useLocation()

  return (
    <div className="min-h-[100dvh] bg-background">
      <header className="sticky top-0 z-20 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80">
        <div className="mx-auto flex max-w-7xl flex-col gap-3 px-4 py-3 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:gap-6">
            <Link className="flex items-center gap-3 transition-opacity hover:opacity-85" to="/dashboard">
              <span className="flex size-9 items-center justify-center rounded-lg bg-primary font-mono text-xs font-semibold text-primary-foreground">
                EC
              </span>
              <span className="flex flex-col leading-tight">
                <span className="font-semibold tracking-tight">E-Commerce Console</span>
                <span className="text-xs text-muted-foreground">Operations demo</span>
              </span>
            </Link>

            <nav
              aria-label="Primary navigation"
              className="flex flex-wrap items-center gap-x-5 gap-y-2 border-t pt-3 lg:border-l lg:border-t-0 lg:pl-6 lg:pt-0"
            >
              {navItems.map((item) => {
                const isActive = item.href === location.pathname

                if (!item.href) {
                  return (
                    <span
                      key={item.label}
                      aria-disabled="true"
                      className="inline-flex h-9 items-center border-b-2 border-transparent text-sm font-medium text-muted-foreground/60"
                    >
                      {item.label}
                    </span>
                  )
                }

                return (
                  <Link
                    key={item.href}
                    aria-current={isActive ? "page" : undefined}
                    className={cn(
                      "inline-flex h-9 items-center border-b-2 px-0.5 text-sm font-medium transition-colors duration-200 hover:border-foreground/30 hover:text-foreground",
                      isActive
                        ? "border-foreground text-foreground"
                        : "border-transparent text-muted-foreground"
                    )}
                    to={item.href}
                  >
                    {item.label}
                  </Link>
                )
              })}
            </nav>
          </div>

          <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-end">
            <LocalDevToolsMenu />
            <UserMenu />
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-6 md:py-8">{children}</main>
    </div>
  )
}
