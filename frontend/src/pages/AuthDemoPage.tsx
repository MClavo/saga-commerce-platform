import { useState } from "react"

import { RoleGuard } from "@/components/auth/RoleGuard"
import { UserMenu } from "@/components/auth/UserMenu"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { formatAuthority } from "@/features/auth/auth-utils"
import { useAuth } from "@/features/auth/use-auth"
import { ApiClientError, apiFetch } from "@/lib/api-client"
import type { Authority } from "@/shared/auth-types"

type Probe = {
  name: string
  path: string
  expected: string
  roles: Authority[]
}

type ProbeResult = Probe & {
  status: "idle" | "success" | "error"
  detail: string
}

const probes: Probe[] = [
  {
    name: "Products",
    path: "/api/v1/products",
    expected: "public GET",
    roles: [],
  },
  {
    name: "Customers",
    path: "/api/v1/customers",
    expected: "ROLE_CUSTOMER_SUPPORT or ROLE_ADMIN",
    roles: ["ROLE_CUSTOMER_SUPPORT", "ROLE_ADMIN"],
  },
  {
    name: "Orders",
    path: "/api/v1/orders",
    expected: "ROLE_ORDER_MANAGER or ROLE_ADMIN",
    roles: ["ROLE_ORDER_MANAGER", "ROLE_ADMIN"],
  },
  {
    name: "Payments",
    path: "/api/v1/payments",
    expected: "ROLE_ORDER_MANAGER or ROLE_ADMIN",
    roles: ["ROLE_ORDER_MANAGER", "ROLE_ADMIN"],
  },
]

function summarizeBody(body: unknown) {
  if (Array.isArray(body)) {
    return `${body.length} records`
  }

  if (typeof body === "string") {
    return body.slice(0, 120)
  }

  if (body && typeof body === "object") {
    return JSON.stringify(body).slice(0, 120)
  }

  return "no body"
}

function statusVariant(status: ProbeResult["status"]) {
  if (status === "success") {
    return "default"
  }

  if (status === "error") {
    return "destructive"
  }

  return "secondary"
}

export function AuthDemoPage() {
  const { user, refreshUser, isLoading, error, hasAnyRole } = useAuth()
  const [results, setResults] = useState<ProbeResult[]>(
    probes.map((probe) => ({ ...probe, status: "idle", detail: "not run" }))
  )
  const [isRunning, setIsRunning] = useState(false)

  async function runProbes() {
    setIsRunning(true)

    const nextResults = await Promise.all(
      probes.map(async (probe): Promise<ProbeResult> => {
        try {
          const body = await apiFetch<unknown>(probe.path)

          return {
            ...probe,
            status: "success",
            detail: summarizeBody(body),
          }
        } catch (caught) {
          if (caught instanceof ApiClientError) {
            return {
              ...probe,
              status: "error",
              detail: `${caught.status} ${caught.statusText}: ${summarizeBody(caught.body)}`,
            }
          }

          return {
            ...probe,
            status: "error",
            detail: caught instanceof Error ? caught.message : "Request failed",
          }
        }
      })
    )

    setResults(nextResults)
    setIsRunning(false)
  }

  return (
    <main className="min-h-screen bg-background p-6">
      <div className="mx-auto flex max-w-6xl flex-col gap-6">
        <header className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
          <div className="flex flex-col gap-2">
            <Badge className="w-fit" variant="secondary">
              Auth iteration
            </Badge>
            <div className="flex flex-col gap-1">
              <h1 className="text-2xl font-semibold tracking-tight">Authentication demo</h1>
              <p className="max-w-2xl text-sm text-muted-foreground">
                Real gateway session, `/auth/me`, role guards, protected API probes. No API mocks. No JWT storage.
              </p>
            </div>
          </div>
          <UserMenu />
        </header>

        <section className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
          <Card>
            <CardHeader>
              <CardTitle>Current session</CardTitle>
              <CardDescription>Loaded from `GET /auth/me` with credentials included.</CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
              {error ? <p className="text-sm text-destructive">{error}</p> : null}
              <div className="grid gap-3 md:grid-cols-2">
                <div className="flex flex-col gap-1 rounded-lg bg-muted p-3">
                  <span className="text-xs text-muted-foreground">Authenticated</span>
                  <span className="font-medium">{user.authenticated ? "true" : "false"}</span>
                </div>
                <div className="flex flex-col gap-1 rounded-lg bg-muted p-3">
                  <span className="text-xs text-muted-foreground">Username</span>
                  <span className="font-medium">{user.authenticated ? user.username : "anonymous"}</span>
                </div>
                <div className="flex flex-col gap-1 rounded-lg bg-muted p-3">
                  <span className="text-xs text-muted-foreground">Name</span>
                  <span className="font-medium">{user.authenticated ? user.name ?? "not provided" : "n/a"}</span>
                </div>
                <div className="flex flex-col gap-1 rounded-lg bg-muted p-3">
                  <span className="text-xs text-muted-foreground">Email</span>
                  <span className="font-medium">{user.authenticated ? user.email ?? "not provided" : "n/a"}</span>
                </div>
              </div>
              <div className="flex flex-wrap gap-2">
                {user.authenticated ? (
                  user.authorities.map((authority) => (
                    <Badge key={authority} variant="outline">
                      {formatAuthority(authority)}
                    </Badge>
                  ))
                ) : (
                  <Badge variant="outline">no roles</Badge>
                )}
              </div>
              <div className="flex flex-wrap gap-2">
                <Button type="button" variant="outline" onClick={() => void refreshUser()} disabled={isLoading}>
                  Refresh `/auth/me`
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Role-gated UI</CardTitle>
              <CardDescription>Frontend mirrors gateway authz. Backend remains source of truth.</CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-3">
              <RoleGuard
                roles={["ROLE_ADMIN"]}
                fallback={<Badge variant="outline">Admin actions hidden</Badge>}
              >
                <Badge>Admin actions visible</Badge>
              </RoleGuard>
              <RoleGuard
                roles={["ROLE_ORDER_MANAGER", "ROLE_ADMIN"]}
                fallback={<Badge variant="outline">Order/payment inspection hidden</Badge>}
              >
                <Badge>Order/payment inspection visible</Badge>
              </RoleGuard>
              <RoleGuard
                roles={["ROLE_CUSTOMER_SUPPORT", "ROLE_ADMIN"]}
                fallback={<Badge variant="outline">Customer support hidden</Badge>}
              >
                <Badge>Customer support visible</Badge>
              </RoleGuard>
              <div className="rounded-lg bg-muted p-3 text-sm text-muted-foreground">
                Product GET stays public. Product mutations, payment confirm/fail remain admin-only and are not executed here.
              </div>
            </CardContent>
          </Card>
        </section>

        <Card>
          <CardHeader>
            <CardTitle>Real API probes</CardTitle>
            <CardDescription>Requests go through Vite proxy to gateway, always with `credentials: include`.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            <div className="flex flex-wrap gap-2">
              <Button type="button" onClick={() => void runProbes()} disabled={isRunning}>
                {isRunning ? "Running probes" : "Run probes"}
              </Button>
            </div>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Endpoint</TableHead>
                  <TableHead>Expected access</TableHead>
                  <TableHead>Current role fit</TableHead>
                  <TableHead>Result</TableHead>
                  <TableHead>Detail</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {results.map((result) => (
                  <TableRow key={result.path}>
                    <TableCell>{result.path}</TableCell>
                    <TableCell>{result.expected}</TableCell>
                    <TableCell>
                      {result.roles.length === 0 || hasAnyRole(result.roles) ? (
                        <Badge variant="secondary">allowed by UI</Badge>
                      ) : (
                        <Badge variant="outline">not allowed by UI</Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge variant={statusVariant(result.status)}>{result.status}</Badge>
                    </TableCell>
                    <TableCell className="max-w-sm truncate">{result.detail}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </main>
  )
}
