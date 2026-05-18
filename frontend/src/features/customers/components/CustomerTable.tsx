import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { EmptyState, ErrorState, TableSkeleton } from "@/components/shared/DataState"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import type { CustomerResponse } from "@/features/customers/customer-api"
import { formatAddressSummary, formatCustomerName } from "@/features/customers/customer-utils"
import type { CustomersState } from "@/features/customers/use-customers-data"

type CustomerTableProps = {
  state: CustomersState
  customers: CustomerResponse[]
  totalCount: number
  onView: (customer: CustomerResponse) => void
  onEdit: (customer: CustomerResponse) => void
  onDelete: (customer: CustomerResponse) => void
}

export function CustomerTable({ state, customers, totalCount, onView, onEdit, onDelete }: CustomerTableProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Customers</CardTitle>
        <CardDescription>Customer records used for order validation and saga creation.</CardDescription>
      </CardHeader>
      <CardContent>
        {state.status === "loading" ? <TableSkeleton columns={5} rows={7} className="grid gap-2 md:grid-cols-[1fr_1.2fr_1.2fr_0.8fr_1fr]" /> : null}
        {state.status === "error" ? <ErrorState title="Customer request failed" message={state.error} /> : null}
        {state.status === "success" && customers.length === 0 ? (
          <EmptyState title="No customers match current search." description="Reset search or create a customer as Customer Support/Admin." />
        ) : null}
        {state.status === "success" && customers.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Address</TableHead>
                <TableHead>ID</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {customers.map((customer) => (
                <TableRow key={customer.id}>
                  <TableCell>
                    <span className="font-medium">{formatCustomerName(customer)}</span>
                  </TableCell>
                  <TableCell>{customer.email}</TableCell>
                  <TableCell>{formatAddressSummary(customer.address)}</TableCell>
                  <TableCell>
                    <Badge variant="outline" className="font-mono">
                      {customer.id}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex justify-end gap-2">
                      <Button type="button" size="sm" variant="outline" onClick={() => onView(customer)}>
                        View
                      </Button>
                      <Button type="button" size="sm" variant="outline" onClick={() => onEdit(customer)}>
                        Edit
                      </Button>
                      <Button type="button" size="sm" variant="destructive" onClick={() => onDelete(customer)}>
                        Delete
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : null}
      </CardContent>
      <CardFooter className="justify-between gap-3">
        <span className="text-sm text-muted-foreground">Showing {customers.length} customers.</span>
        <span className="font-mono text-sm text-muted-foreground">Total {totalCount}</span>
      </CardFooter>
    </Card>
  )
}
