import type { ReactNode } from "react"

import { useAuth } from "@/features/auth/use-auth"
import type { Authority } from "@/shared/auth-types"

type RoleGuardProps = {
  roles: Authority[]
  children: ReactNode
  fallback?: ReactNode
}

export function RoleGuard({ roles, children, fallback = null }: RoleGuardProps) {
  const { hasAnyRole } = useAuth()

  if (!hasAnyRole(roles)) {
    return fallback
  }

  return children
}
