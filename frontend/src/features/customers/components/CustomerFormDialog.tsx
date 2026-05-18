import { useState, type FormEvent } from "react"

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import type { CustomerRequest, CustomerResponse } from "@/features/customers/customer-api"
import { formatCustomerApiError } from "@/features/customers/customer-utils"

type CustomerFormDialogProps = {
  mode: "create" | "edit"
  customer?: CustomerResponse
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (request: CustomerRequest) => Promise<void>
}

type CustomerFormValues = {
  firstname: string
  lastname: string
  email: string
  street: string
  houseNumber: string
  zipCode: string
}

type CustomerFormErrors = Partial<Record<keyof CustomerFormValues, string>>

export function CustomerFormDialog({ mode, customer, open, onOpenChange, onSubmit }: CustomerFormDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {open ? (
        <CustomerFormContent
          key={customer?.id ?? mode}
          mode={mode}
          customer={customer}
          onCancel={() => onOpenChange(false)}
          onSubmit={async (request) => {
            await onSubmit(request)
            onOpenChange(false)
          }}
        />
      ) : null}
    </Dialog>
  )
}

function CustomerFormContent({
  mode,
  customer,
  onCancel,
  onSubmit,
}: {
  mode: "create" | "edit"
  customer?: CustomerResponse
  onCancel: () => void
  onSubmit: (request: CustomerRequest) => Promise<void>
}) {
  const [values, setValues] = useState<CustomerFormValues>({
    firstname: customer?.firstname ?? "",
    lastname: customer?.lastname ?? "",
    email: customer?.email ?? "",
    street: customer?.address?.street ?? "",
    houseNumber: customer?.address?.houseNumber ?? "",
    zipCode: customer?.address?.zipCode ?? "",
  })
  const [errors, setErrors] = useState<CustomerFormErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  function updateField(field: keyof CustomerFormValues, value: string) {
    setValues((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: undefined }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFormError(null)

    const parsed = parseCustomerForm(values)

    if ("errors" in parsed) {
      setErrors(parsed.errors)
      return
    }

    setErrors({})
    setIsSubmitting(true)

    try {
      await onSubmit(parsed.request)
    } catch (caught) {
      setFormError(formatCustomerApiError(caught, "Customer mutation failed"))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <DialogContent className="sm:max-w-2xl">
      <DialogHeader>
        <DialogTitle>{mode === "create" ? "Create customer" : "Edit customer"}</DialogTitle>
        <DialogDescription>
          {mode === "create" ? "Create a customer that can be validated during order creation." : "Update customer identity and optional address."}
        </DialogDescription>
      </DialogHeader>

      <form className="flex flex-col gap-5" onSubmit={(event) => void handleSubmit(event)}>
        <FieldGroup className="sm:grid sm:grid-cols-2">
          <Field data-invalid={Boolean(errors.firstname)}>
            <FieldLabel htmlFor="customer-firstname">First name</FieldLabel>
            <Input
              id="customer-firstname"
              value={values.firstname}
              aria-invalid={Boolean(errors.firstname)}
              onChange={(event) => updateField("firstname", event.target.value)}
            />
            <FieldDescription>Required customer first name.</FieldDescription>
            <FieldError>{errors.firstname}</FieldError>
          </Field>

          <Field data-invalid={Boolean(errors.lastname)}>
            <FieldLabel htmlFor="customer-lastname">Last name</FieldLabel>
            <Input
              id="customer-lastname"
              value={values.lastname}
              aria-invalid={Boolean(errors.lastname)}
              onChange={(event) => updateField("lastname", event.target.value)}
            />
            <FieldDescription>Required customer last name.</FieldDescription>
            <FieldError>{errors.lastname}</FieldError>
          </Field>
        </FieldGroup>

        <FieldGroup>
          <Field data-invalid={Boolean(errors.email)}>
            <FieldLabel htmlFor="customer-email">Email</FieldLabel>
            <Input
              id="customer-email"
              type="email"
              value={values.email}
              aria-invalid={Boolean(errors.email)}
              onChange={(event) => updateField("email", event.target.value)}
            />
            <FieldDescription>Required email; backend validates email format too.</FieldDescription>
            <FieldError>{errors.email}</FieldError>
          </Field>
        </FieldGroup>

        <FieldSet>
          <FieldLegend>Optional address</FieldLegend>
          <FieldDescription>Leave all address fields blank to store no address.</FieldDescription>
          <FieldGroup className="sm:grid sm:grid-cols-3">
            <Field>
              <FieldLabel htmlFor="customer-street">Street</FieldLabel>
              <Input id="customer-street" value={values.street} onChange={(event) => updateField("street", event.target.value)} />
            </Field>
            <Field>
              <FieldLabel htmlFor="customer-house-number">House number</FieldLabel>
              <Input id="customer-house-number" value={values.houseNumber} onChange={(event) => updateField("houseNumber", event.target.value)} />
            </Field>
            <Field>
              <FieldLabel htmlFor="customer-zip-code">Zip code</FieldLabel>
              <Input id="customer-zip-code" value={values.zipCode} onChange={(event) => updateField("zipCode", event.target.value)} />
            </Field>
          </FieldGroup>
        </FieldSet>

        {formError ? (
          <Alert variant="destructive">
            <AlertTitle>Customer save failed</AlertTitle>
            <AlertDescription>{formError}</AlertDescription>
          </Alert>
        ) : null}

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Saving" : "Save customer"}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  )
}

function parseCustomerForm(values: CustomerFormValues): { request: CustomerRequest } | { errors: CustomerFormErrors } {
  const firstname = values.firstname.trim()
  const lastname = values.lastname.trim()
  const email = values.email.trim()
  const errors: CustomerFormErrors = {}

  if (!firstname) {
    errors.firstname = "First name is required."
  }

  if (!lastname) {
    errors.lastname = "Last name is required."
  }

  if (!email) {
    errors.email = "Email is required."
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errors.email = "Enter a valid email address."
  }

  if (Object.keys(errors).length > 0) {
    return { errors }
  }

  const street = values.street.trim()
  const houseNumber = values.houseNumber.trim()
  const zipCode = values.zipCode.trim()
  const address = street || houseNumber || zipCode ? { street: street || undefined, houseNumber: houseNumber || undefined, zipCode: zipCode || undefined } : null

  return { request: { firstname, lastname, email, address } }
}
