import { Link } from "react-router-dom"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { useAuth } from "@/features/auth/use-auth"

const sagaSteps = [
  "Validate customer",
  "Reserve product stock",
  "Request payment",
  "Resolve order state",
  "Notify customer",
]

export function SagaFlowCard() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole("ROLE_ADMIN")

  return (
    <Card>
      <CardHeader>
        <div className="flex flex-col gap-2">
          <Badge className="w-fit" variant="secondary">
            Order Processing Saga
          </Badge>
          <CardTitle>Backend workflow made visible</CardTitle>
          <CardDescription>
            The dashboard summarizes the services that participate in the order lifecycle. The guided saga demo comes next.
          </CardDescription>
        </div>
      </CardHeader>
      <CardContent className="grid gap-4 lg:grid-cols-[1.4fr_0.6fr]">
        <div className="grid gap-2 sm:grid-cols-5">
          {sagaSteps.map((step, index) => (
            <div key={step} className="flex flex-col gap-2 rounded-lg bg-muted p-3">
              <span className="font-mono text-xs text-muted-foreground">0{index + 1}</span>
              <span className="text-sm font-medium leading-snug">{step}</span>
            </div>
          ))}
        </div>
        <div className="flex flex-col justify-between gap-3 rounded-lg border p-4">
          <p className="text-sm text-muted-foreground">
            {isAdmin
              ? "Create a demo order, then jump directly into Order Flow to watch the saga resolve."
              : "Start Saga Demo requires Admin because it crosses customer, order, and payment demo actions."}
          </p>
          {isAdmin ? (
            <Button asChild type="button">
              <Link to="/saga-demo">Start Saga Demo</Link>
            </Button>
          ) : (
            <Button disabled type="button">
              Start Saga Demo
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
