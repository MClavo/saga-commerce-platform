import { LogOut } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { DemoAccountsDialog } from "@/components/auth/DemoAccountsDialog"
import { useAuth } from "@/features/auth/use-auth"

export function UserMenu() {
  const { user, login, logout, isLoading } = useAuth()

  if (isLoading) {
    return <Badge className="h-9 px-3" variant="secondary">Checking session</Badge>
  }

  if (!user.authenticated) {
    return (
      <div className="flex items-center gap-2">
        <DemoAccountsDialog />
        <Button type="button" onClick={login}>
          Login with Keycloak
        </Button>
      </div>
    )
  }

  return (
    <div className="flex flex-wrap items-center justify-end gap-2 sm:flex-nowrap">
      <div className="flex h-9 min-w-0 max-w-56 items-center gap-3 rounded-lg border border-border bg-background px-3">
        <span className="size-2 rounded-full bg-primary" aria-hidden="true" />
        <div className="flex min-w-0 flex-col leading-tight">
          <span className="truncate text-sm font-medium">{user.name ?? user.username}</span>
          {user.email ? (
            <span className="truncate text-xs text-muted-foreground">{user.email}</span>
          ) : null}
        </div>
      </div>
      <Button className="h-9 px-3" type="button" variant="ghost" onClick={() => void logout()}>
        <LogOut data-icon="inline-start" />
        Logout / switch user
      </Button>
    </div>
  )
}
