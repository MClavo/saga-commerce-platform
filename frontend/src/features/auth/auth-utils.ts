import type { AuthUser, Authority } from "@/shared/auth-types"

export const demoAccounts = [
  {
    username: "admin",
    password: "admin",
    roles: ["ROLE_ADMIN", "ROLE_ORDER_MANAGER", "ROLE_CUSTOMER_SUPPORT"],
  },
  {
    username: "orders",
    password: "orders",
    roles: ["ROLE_ORDER_MANAGER"],
  },
  {
    username: "support",
    password: "support",
    roles: ["ROLE_CUSTOMER_SUPPORT"],
  },
] satisfies Array<{ username: string; password: string; roles: Authority[] }>

export function getUserAuthorities(user: AuthUser): Authority[] {
  return user.authenticated ? user.authorities : []
}

export function hasAuthority(user: AuthUser, authority: Authority) {
  return getUserAuthorities(user).includes(authority)
}

export function hasAnyAuthority(user: AuthUser, authorities: Authority[]) {
  return authorities.some((authority) => hasAuthority(user, authority))
}

export function formatAuthority(authority: Authority) {
  return authority.replace("ROLE_", "").replaceAll("_", " ").toLowerCase()
}
