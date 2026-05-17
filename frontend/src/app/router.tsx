import { createBrowserRouter, Navigate } from "react-router-dom"

import { ProtectedRoute } from "@/components/auth/ProtectedRoute"
import { RootRedirect } from "@/app/RootRedirect"
import { DashboardPage } from "@/features/dashboard/DashboardPage"
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
    path: "/dashboard",
    element: (
      <ProtectedRoute>
        <DashboardPage />
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
