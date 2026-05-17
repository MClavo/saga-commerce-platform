import { createBrowserRouter, Navigate } from "react-router-dom"

import { ProtectedRoute } from "@/components/auth/ProtectedRoute"
import { RootRedirect } from "@/app/RootRedirect"
import { AuthDemoPage } from "@/pages/AuthDemoPage"
import { LoginPage } from "@/pages/LoginPage"
import { UnauthorizedPage } from "@/pages/UnauthorizedPage"

export const router = createBrowserRouter([
  {
    path: "/",
    element: <RootRedirect />,
  },
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/auth-demo",
    element: (
      <ProtectedRoute>
        <AuthDemoPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/auth-demo/admin",
    element: (
      <ProtectedRoute roles={["ROLE_ADMIN"]}>
        <AuthDemoPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/auth-demo/orders",
    element: (
      <ProtectedRoute roles={["ROLE_ORDER_MANAGER", "ROLE_ADMIN"]}>
        <AuthDemoPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/auth-demo/customers",
    element: (
      <ProtectedRoute roles={["ROLE_CUSTOMER_SUPPORT", "ROLE_ADMIN"]}>
        <AuthDemoPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/unauthorized",
    element: <UnauthorizedPage />,
  },
  {
    path: "*",
    element: <Navigate replace to="/" />,
  },
])
