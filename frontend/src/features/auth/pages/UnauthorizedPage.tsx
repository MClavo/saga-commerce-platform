import { Link } from "react-router-dom"

import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"

export function UnauthorizedPage() {
  return (
    <main className="flex min-h-[100dvh] items-center justify-center bg-background p-6">
      <Card className="w-full max-w-lg">
        <CardHeader>
          <CardTitle>Unauthorized</CardTitle>
          <CardDescription>Current account lacks required role for this route.</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">
            Logout, then login as account with matching Keycloak role.
          </p>
        </CardContent>
        <CardFooter>
          <Button asChild variant="outline">
            <Link to="/auth-demo">Back to auth demo</Link>
          </Button>
        </CardFooter>
      </Card>
    </main>
  )
}
