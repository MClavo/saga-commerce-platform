import { createContext } from "react"

import type { AuthUser, Authority } from "@/shared/auth-types"

export type AuthContextValue = {
  user: AuthUser
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null
  refreshUser: () => Promise<void>
  login: () => void
  logout: () => Promise<void>
  hasRole: (authority: Authority) => boolean
  hasAnyRole: (authorities: Authority[]) => boolean
}

export const AuthContext = createContext<AuthContextValue | null>(null)
