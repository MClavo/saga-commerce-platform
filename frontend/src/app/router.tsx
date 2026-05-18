import { createBrowserRouter, Navigate } from "react-router-dom"

import { ProtectedRoute } from "@/features/auth/components/ProtectedRoute"
import { RootRedirect } from "@/app/RootRedirect"
import { CustomersPage } from "@/features/customers/CustomersPage"
import { DashboardPage } from "@/features/dashboard/DashboardPage"
import { OrderFlowPage } from "@/features/orders/OrderFlowPage"
import { OrdersPage } from "@/features/orders/OrdersPage"
import { CatalogPage } from "@/features/products/CatalogPage"
import { LoginPage } from "@/features/auth/pages/LoginPage"
import { UnauthorizedPage } from "@/features/auth/pages/UnauthorizedPage"

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
    path: "/catalog",
    element: (
      <ProtectedRoute>
        <CatalogPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/customers",
    element: (
      <ProtectedRoute roles={["ROLE_CUSTOMER_SUPPORT", "ROLE_ADMIN"]}>
        <CustomersPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/orders",
    element: (
      <ProtectedRoute roles={["ROLE_ORDER_MANAGER", "ROLE_ADMIN"]}>
        <OrdersPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/orders/:id/flow",
    element: (
      <ProtectedRoute roles={["ROLE_ORDER_MANAGER", "ROLE_ADMIN"]}>
        <OrderFlowPage />
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
