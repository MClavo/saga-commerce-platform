import { useEffect, useState, type ReactNode } from "react"

import {
  getCurrentUser,
  login as startLogin,
  logout as submitLogout,
} from "@/features/auth/auth-api"
import { AuthContext, type AuthContextValue } from "@/features/auth/auth-store"
import { hasAnyAuthority, hasAuthority } from "@/features/auth/auth-utils"
import type { AuthUser } from "@/shared/auth-types"

const anonymousUser: AuthUser = { authenticated: false }

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser>(anonymousUser)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  async function refreshUser() {
    setIsLoading(true)
    setError(null)

    try {
      setUser(await getCurrentUser())
    } catch (caught) {
      setUser(anonymousUser)
      setError(caught instanceof Error ? caught.message : "Auth check failed")
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    let active = true

    async function loadUser() {
      setIsLoading(true)
      setError(null)

      try {
        const currentUser = await getCurrentUser()

        if (active) {
          setUser(currentUser)
        }
      } catch (caught) {
        if (active) {
          setUser(anonymousUser)
          setError(caught instanceof Error ? caught.message : "Auth check failed")
        }
      } finally {
        if (active) {
          setIsLoading(false)
        }
      }
    }

    void loadUser()

    return () => {
      active = false
    }
  }, [])

  const value: AuthContextValue = {
    user,
    isAuthenticated: user.authenticated,
    isLoading,
    error,
    refreshUser,
    login: startLogin,
    logout: submitLogout,
    hasRole: (authority) => hasAuthority(user, authority),
    hasAnyRole: (authorities) => hasAnyAuthority(user, authorities),
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
