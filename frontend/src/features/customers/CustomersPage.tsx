import { useMemo, useState } from "react"

import { AppShell } from "@/components/layout/AppShell"
import { MetricCard } from "@/components/shared/MetricCard"
import { PageHeader } from "@/components/shared/PageHeader"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { CustomerDetailSheet } from "@/features/customers/components/CustomerDetailSheet"
import { CustomerFilters } from "@/features/customers/components/CustomerFilters"
import { CustomerFormDialog } from "@/features/customers/components/CustomerFormDialog"
import { CustomerTable } from "@/features/customers/components/CustomerTable"
import { DeleteCustomerDialog } from "@/features/customers/components/DeleteCustomerDialog"
import type { CustomerRequest, CustomerResponse } from "@/features/customers/customer-api"
import { compareCustomers, getCustomerSearchText, hasAddress } from "@/features/customers/customer-utils"
import { useCustomersData } from "@/features/customers/use-customers-data"

export function CustomersPage() {
  const { customers, isRefreshing, refresh, createCustomer, updateCustomer, deleteCustomer } = useCustomersData()
  const [search, setSearch] = useState("")
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerResponse | null>(null)
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [editingCustomer, setEditingCustomer] = useState<CustomerResponse | null>(null)
  const [deletingCustomer, setDeletingCustomer] = useState<CustomerResponse | null>(null)

  const visibleCustomers = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase()
    const sortedCustomers = [...customers.data].sort(compareCustomers)

    if (!normalizedSearch) {
      return sortedCustomers
    }

    return sortedCustomers.filter((customer) => getCustomerSearchText(customer).includes(normalizedSearch))
  }, [customers.data, search])

  const metrics = useMemo(() => {
    const withAddress = customers.data.filter((customer) => hasAddress(customer.address)).length

    return {
      total: customers.data.length,
      withAddress,
      missingAddress: customers.data.length - withAddress,
    }
  }, [customers.data])

  function resetSearch() {
    setSearch("")
  }

  async function submitUpdateCustomer(request: CustomerRequest) {
    if (!editingCustomer) {
      return
    }

    await updateCustomer(editingCustomer.id, request)

    if (selectedCustomer?.id === editingCustomer.id) {
      setSelectedCustomer(null)
    }
  }

  async function submitDeleteCustomer() {
    if (!deletingCustomer) {
      return
    }

    await deleteCustomer(deletingCustomer.id)

    if (selectedCustomer?.id === deletingCustomer.id) {
      setSelectedCustomer(null)
    }
  }

  return (
    <AppShell>
      <div className="flex flex-col gap-6">
        <PageHeader
          eyebrow="Customers"
          title="Manage customers used by order validation."
          description="Customer records stay focused: identity, email, and optional address. They support the saga but should not dominate the demo."
          badge={<Badge variant="outline">Restricted: Customer Support / Admin</Badge>}
          actions={
            <>
            <Button type="button" variant="outline" onClick={() => void refresh()} disabled={isRefreshing}>
              {isRefreshing ? "Refreshing" : "Refresh"}
            </Button>
            <Button type="button" onClick={() => setIsCreateOpen(true)}>
              Create Customer
            </Button>
            </>
          }
        />

        <section className="grid gap-3 md:grid-cols-2 xl:grid-cols-[1.2fr_0.8fr_1fr_0.7fr]">
          <MetricCard label="Total customers" value={metrics.total} />
          <MetricCard label="With address" value={metrics.withAddress} />
          <MetricCard label="Missing address" value={metrics.missingAddress} />
        </section>

        <CustomerFilters search={search} onSearchChange={setSearch} onReset={resetSearch} />

        <CustomerTable
          state={customers}
          customers={visibleCustomers}
          totalCount={customers.data.length}
          onView={setSelectedCustomer}
          onEdit={setEditingCustomer}
          onDelete={setDeletingCustomer}
        />
      </div>

      <CustomerDetailSheet
        customer={selectedCustomer}
        open={Boolean(selectedCustomer)}
        onOpenChange={(open) => !open && setSelectedCustomer(null)}
        onEdit={setEditingCustomer}
        onDelete={setDeletingCustomer}
      />
      <CustomerFormDialog
        mode="create"
        open={isCreateOpen}
        onOpenChange={setIsCreateOpen}
        onSubmit={(request) => createCustomer(request)}
      />
      <CustomerFormDialog
        mode="edit"
        customer={editingCustomer ?? undefined}
        open={Boolean(editingCustomer)}
        onOpenChange={(open) => !open && setEditingCustomer(null)}
        onSubmit={submitUpdateCustomer}
      />
      <DeleteCustomerDialog
        customer={deletingCustomer}
        open={Boolean(deletingCustomer)}
        onOpenChange={(open) => !open && setDeletingCustomer(null)}
        onSubmit={submitDeleteCustomer}
      />
    </AppShell>
  )
}
