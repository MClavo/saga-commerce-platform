import { Navigate, useLocation } from "react-router-dom"

import { DemoAccountsDialog } from "@/features/auth/components/DemoAccountsDialog"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuth } from "@/features/auth/use-auth"

type LoginLocationState = {
  from?: {
    pathname?: string
  }
}

export function LoginPage() {
  const { isAuthenticated, isLoading, login, error } = useAuth()
  const location = useLocation()
  const state = location.state as LoginLocationState | null
  const nextPath = state?.from?.pathname ?? "/dashboard"

  if (isLoading) {
    return (
      <main className="flex min-h-[100dvh] items-center justify-center bg-background p-6">
        <Card className="w-full max-w-xl">
          <CardHeader>
            <CardTitle>Checking session</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            <Skeleton className="h-4 w-2/3" />
            <Skeleton className="h-4 w-1/2" />
          </CardContent>
        </Card>
      </main>
    )
  }

  if (isAuthenticated) {
    return <Navigate replace to={nextPath} />
  }

  return (
    <main className="flex min-h-[100dvh] items-center justify-center bg-background p-6">
      <Card className="w-full max-w-xl">
        <CardHeader>
          <Badge className="w-fit" variant="secondary">
            Gateway BFF auth
          </Badge>
          <CardTitle>Sign in with Keycloak</CardTitle>
          <CardDescription>
            Gateway owns OAuth2 login, session cookie, CSRF, token relay. React stores no JWT.
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          {error ? <p className="text-sm text-destructive">{error}</p> : null}
          <p className="text-sm text-muted-foreground">
            Use demo accounts to test admin, order manager, customer support role rendering.
          </p>
        </CardContent>
        <CardFooter className="gap-2">
          <Button type="button" onClick={login}>
            Login with Keycloak
          </Button>
          <DemoAccountsDialog />
        </CardFooter>
      </Card>
    </main>
  )
}
