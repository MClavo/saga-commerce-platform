import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { DemoAccountsDialog } from "@/components/auth/DemoAccountsDialog"
import { useAuth } from "@/features/auth/use-auth"

export function UserMenu() {
  const { user, login, logout, isLoading } = useAuth()

  if (isLoading) {
    return <Badge variant="secondary">Checking session</Badge>
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
    <div className="flex flex-wrap items-center justify-end gap-2">
      <div className="flex flex-col items-end gap-1">
        <span className="text-sm font-medium">{user.name ?? user.username}</span>
        {user.email ? (
          <span className="text-xs text-muted-foreground">{user.email}</span>
        ) : null}
      </div>
      <Button type="button" variant="outline" onClick={() => void logout()}>
        Logout / switch user
      </Button>
    </div>
  )
}
