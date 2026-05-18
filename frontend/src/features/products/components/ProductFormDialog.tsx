import { useState, type FormEvent } from "react"

import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import type { CreateProductRequest, ProductResponse, UpdateProductRequest } from "@/features/products/product-api"

type ProductFormDialogProps = {
  mode: "create" | "edit"
  product?: ProductResponse
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (request: CreateProductRequest | UpdateProductRequest) => Promise<void>
}

type ProductFormValues = {
  name: string
  description: string
  availableQuantity: string
  price: string
  categoryId: string
}

export function ProductFormDialog({ mode, product, open, onOpenChange, onSubmit }: ProductFormDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {open ? (
        <ProductFormContent
          key={product?.id ?? mode}
          mode={mode}
          product={product}
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

function ProductFormContent({
  mode,
  product,
  onCancel,
  onSubmit,
}: {
  mode: "create" | "edit"
  product?: ProductResponse
  onCancel: () => void
  onSubmit: (request: CreateProductRequest | UpdateProductRequest) => Promise<void>
}) {
  const [values, setValues] = useState<ProductFormValues>({
    name: product?.name ?? "",
    description: product?.description ?? "",
    availableQuantity: product?.availableQuantity.toString() ?? "0",
    price: product?.price.toString() ?? "0",
    categoryId: product?.categoryId.toString() ?? "",
  })
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  function updateField(field: keyof ProductFormValues, value: string) {
    setValues((current) => ({ ...current, [field]: value }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    const parsed = parseProductForm(values, mode)

    if (typeof parsed === "string") {
      setError(parsed)
      return
    }

    setIsSubmitting(true)

    try {
      await onSubmit(parsed)
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Product mutation failed")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <DialogContent className="sm:max-w-xl">
      <DialogHeader>
        <DialogTitle>{mode === "create" ? "Create product" : "Edit product"}</DialogTitle>
        <DialogDescription>
          {mode === "create" ? "Create catalog item and initial available stock." : "Update catalog fields. Stock changes use adjustment action."}
        </DialogDescription>
      </DialogHeader>

      <form className="flex flex-col gap-4" onSubmit={(event) => void handleSubmit(event)}>
        <FieldGroup className="sm:grid sm:grid-cols-2">
          <Field>
            <FieldLabel>Name</FieldLabel>
            <Input value={values.name} onChange={(event) => updateField("name", event.target.value)} />
            <FieldDescription>Required product name.</FieldDescription>
          </Field>
          <Field>
            <FieldLabel>Category ID</FieldLabel>
            <Input value={values.categoryId} inputMode="numeric" onChange={(event) => updateField("categoryId", event.target.value)} />
            <FieldDescription>Existing numeric category id.</FieldDescription>
          </Field>
          <Field>
            <FieldLabel>Price</FieldLabel>
            <Input value={values.price} inputMode="decimal" onChange={(event) => updateField("price", event.target.value)} />
            <FieldDescription>EUR demo price; backend stores numeric amount.</FieldDescription>
          </Field>
          {mode === "create" ? (
            <Field>
              <FieldLabel>Available quantity</FieldLabel>
              <Input
                value={values.availableQuantity}
                inputMode="numeric"
                onChange={(event) => updateField("availableQuantity", event.target.value)}
              />
              <FieldDescription>Initial available stock.</FieldDescription>
            </Field>
          ) : null}
        </FieldGroup>

        <Field>
          <FieldLabel>Description</FieldLabel>
          <Input value={values.description} onChange={(event) => updateField("description", event.target.value)} />
          <FieldDescription>Short operational catalog description.</FieldDescription>
        </Field>

        <FieldError>{error}</FieldError>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Saving" : "Save"}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  )
}

function parseProductForm(values: ProductFormValues, mode: "create" | "edit") {
  const name = values.name.trim()
  const description = values.description.trim()
  const price = Number(values.price)
  const availableQuantity = Number(values.availableQuantity)
  const categoryId = Number(values.categoryId)

  if (!name) {
    return "Name is required."
  }

  if (!Number.isFinite(price) || price < 0) {
    return "Price must be zero or greater."
  }

  if (!Number.isInteger(categoryId) || categoryId <= 0) {
    return "Category ID must be a positive integer."
  }

  if (mode === "create") {
    if (!Number.isInteger(availableQuantity) || availableQuantity < 0) {
      return "Available quantity must be a non-negative integer."
    }

    return { name, description, availableQuantity, price, categoryId }
  }

  return { name, description, price, categoryId }
}
