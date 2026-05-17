export type Role =
  | "ROLE_ADMIN"
  | "ROLE_ORDER_MANAGER"
  | "ROLE_CUSTOMER_SUPPORT"

export type Authority = string

export type AuthenticatedUser = {
  authenticated: true
  subject?: string
  username: string
  email?: string
  name?: string
  authorities: Authority[]
  roles: Role[]
}

export type AnonymousUser = {
  authenticated: false
}

export type AuthUser = AuthenticatedUser | AnonymousUser
