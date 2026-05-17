import type { Authority } from "@/shared/auth-types"

const roleLabels: Record<string, string> = {
  ROLE_ADMIN: "Admin",
  ROLE_ORDER_MANAGER: "Order Manager",
  ROLE_CUSTOMER_SUPPORT: "Customer Support",
}

export function formatRoleLabel(authority: Authority) {
  return roleLabels[authority] ?? authority.replace("ROLE_", "").replaceAll("_", " ")
}

export function formatRequiredRoles(authorities: Authority[]) {
  return authorities.map(formatRoleLabel).join(" or ")
}
