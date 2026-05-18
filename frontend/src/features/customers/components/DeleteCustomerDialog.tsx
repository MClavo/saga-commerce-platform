import { useState, type MouseEvent } from "react"

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import type { CustomerResponse } from "@/features/customers/customer-api"
import { formatCustomerApiError, formatCustomerName } from "@/features/customers/customer-utils"

type DeleteCustomerDialogProps = {
  customer: CustomerResponse | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: () => Promise<void>
}

export function DeleteCustomerDialog({ customer, open, onOpenChange, onSubmit }: DeleteCustomerDialogProps) {
  const [error, setError] = useState<string | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  async function handleDelete(event: MouseEvent<HTMLButtonElement>) {
    event.preventDefault()

    if (!customer) {
      return
    }

    setError(null)
    setIsDeleting(true)

    try {
      await onSubmit()
      onOpenChange(false)
    } catch (caught) {
      setError(formatCustomerApiError(caught, "Customer delete failed"))
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <AlertDialog
      open={open}
      onOpenChange={(nextOpen) => {
        if (!nextOpen) {
          setError(null)
        }

        onOpenChange(nextOpen)
      }}
    >
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Delete customer?</AlertDialogTitle>
          <AlertDialogDescription>
            This hard deletes {customer ? formatCustomerName(customer) : "this customer"}. Existing Orders may still reference deleted customerId and customer snapshot.
          </AlertDialogDescription>
        </AlertDialogHeader>

        {error ? (
          <Alert variant="destructive">
            <AlertTitle>Customer delete failed</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        ) : null}

        <AlertDialogFooter>
          <AlertDialogCancel disabled={isDeleting}>Cancel</AlertDialogCancel>
          <AlertDialogAction variant="destructive" disabled={isDeleting} onClick={(event) => void handleDelete(event)}>
            {isDeleting ? "Deleting" : "Delete customer"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
