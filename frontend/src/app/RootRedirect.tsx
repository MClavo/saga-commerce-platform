import { Navigate } from "react-router-dom"

import { useAuth } from "@/features/auth/use-auth"

export function RootRedirect() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return null
  }

  return <Navigate replace to={isAuthenticated ? "/dashboard" : "/login"} />
}
