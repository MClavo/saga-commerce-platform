import { apiFetch } from "@/lib/api-client"
import type { AuthUser } from "@/shared/auth-types"

export function login() {
  window.location.href = "/auth/login"
}

export function getCurrentUser() {
  return apiFetch<AuthUser>("/auth/me")
}

export async function logout() {
  const form = document.createElement("form")

  form.method = "POST"
  form.action = "/auth/logout"

  document.body.appendChild(form)
  form.submit()
}
