import { Button } from "@/components/ui/button"
import { DetailItem } from "@/components/shared/DetailItem"
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from "@/components/ui/sheet"
import type { CustomerResponse } from "@/features/customers/customer-api"
import { formatAddressSummary, formatCustomerName } from "@/features/customers/customer-utils"

type CustomerDetailSheetProps = {
  customer: CustomerResponse | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onEdit: (customer: CustomerResponse) => void
  onDelete: (customer: CustomerResponse) => void
}

export function CustomerDetailSheet({ customer, open, onOpenChange, onEdit, onDelete }: CustomerDetailSheetProps) {
  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="sm:max-w-xl">
        <SheetHeader>
          <SheetTitle>{customer ? formatCustomerName(customer) : "Customer detail"}</SheetTitle>
          <SheetDescription>Customer domain record used during order validation.</SheetDescription>
        </SheetHeader>
        {customer ? (
          <div className="flex flex-col gap-4 px-4 pb-4">
            <div className="grid gap-3 sm:grid-cols-2">
              <DetailItem label="First name" value={customer.firstname} />
              <DetailItem label="Last name" value={customer.lastname} />
              <DetailItem label="Email" value={customer.email} />
              <DetailItem label="Customer ID" value={customer.id} mono />
            </div>
            <div className="flex flex-col gap-2 rounded-xl border p-4">
              <span className="text-sm font-medium">Address</span>
              <span className="text-sm text-muted-foreground">{formatAddressSummary(customer.address)}</span>
            </div>
          </div>
        ) : null}
        {customer ? (
          <SheetFooter>
            <Button type="button" variant="outline" onClick={() => onEdit(customer)}>
              Edit customer
            </Button>
            <Button type="button" variant="destructive" onClick={() => onDelete(customer)}>
              Delete customer
            </Button>
          </SheetFooter>
        ) : null}
      </SheetContent>
    </Sheet>
  )
}
