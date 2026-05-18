import { useEffect, useMemo, useState } from "react"
import { Link, useNavigate } from "react-router-dom"

import { AppShell } from "@/components/layout/AppShell"
import { ErrorState, EmptyState, TableSkeleton } from "@/components/shared/DataState"
import { DetailItem } from "@/components/shared/DetailItem"
import { PageHeader } from "@/components/shared/PageHeader"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldDescription, FieldGroup, FieldLabel, FieldLegend, FieldSet } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import type { CustomerResponse } from "@/features/customers/customer-api"
import { compareCustomers, formatCustomerName, getCustomerSearchText } from "@/features/customers/customer-utils"
import { useCustomersData } from "@/features/customers/use-customers-data"
import { createOrder, type PaymentMethod } from "@/features/orders/order-api"
import { formatOrderApiError, formatPaymentMethod } from "@/features/orders/order-utils"
import { ProductStockBadge } from "@/features/products/components/ProductStockBadge"
import type { ProductResponse } from "@/features/products/product-api"
import { useProductsData } from "@/features/products/use-products-data"
import { formatMoney } from "@/shared/formatters"

type SagaDemoDraft = {
  reference: string
  selectedCustomerId: string | null
  productQuantities: Record<string, number>
  paymentMethod: PaymentMethod | null
  step: number
}

type SelectedLine = {
  product: ProductResponse
  quantity: number
  subtotal: number
}

const draftStorageKey = "ecommerce.saga-demo.draft"
const maxQuantity = 999
const paymentMethods: PaymentMethod[] = ["CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "VISA", "MASTERCARD"]
const steps = ["Select Customer", "Select Products", "Select Payment", "Review And Create"]

function createInitialDraft(): SagaDemoDraft {
  return {
    reference: generateReference(),
    selectedCustomerId: null,
    productQuantities: {},
    paymentMethod: null,
    step: 0,
  }
}

function generateReference() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, "0")
  const day = String(now.getDate()).padStart(2, "0")
  const hour = String(now.getHours()).padStart(2, "0")
  const minute = String(now.getMinutes()).padStart(2, "0")
  const second = String(now.getSeconds()).padStart(2, "0")
  const random = Math.random().toString(36).slice(2, 8).toUpperCase()

  return `DEMO-${year}${month}${day}-${hour}${minute}${second}-${random}`
}

function readStoredDraft() {
  try {
    const stored = sessionStorage.getItem(draftStorageKey)

    if (!stored) {
      return createInitialDraft()
    }

    const parsed = JSON.parse(stored) as Partial<SagaDemoDraft>

    return {
      reference: typeof parsed.reference === "string" && parsed.reference ? parsed.reference : generateReference(),
      selectedCustomerId: typeof parsed.selectedCustomerId === "string" ? parsed.selectedCustomerId : null,
      productQuantities: sanitizeQuantities(parsed.productQuantities),
      paymentMethod: isPaymentMethod(parsed.paymentMethod) ? parsed.paymentMethod : null,
      step: clampStep(parsed.step),
    }
  } catch {
    return createInitialDraft()
  }
}

function sanitizeQuantities(value: unknown) {
  if (!value || typeof value !== "object") {
    return {}
  }

  return Object.fromEntries(
    Object.entries(value as Record<string, unknown>)
      .map(([productId, quantity]) => [productId, clampQuantity(quantity)] as const)
      .filter(([, quantity]) => quantity > 0)
  )
}

function clampQuantity(value: unknown) {
  const parsed = typeof value === "number" ? value : Number(value)

  if (!Number.isFinite(parsed)) {
    return 0
  }

  return Math.min(maxQuantity, Math.max(0, Math.floor(parsed)))
}

function clampStep(value: unknown) {
  const parsed = typeof value === "number" ? value : Number(value)

  if (!Number.isFinite(parsed)) {
    return 0
  }

  return Math.min(steps.length - 1, Math.max(0, Math.floor(parsed)))
}

function isPaymentMethod(value: unknown): value is PaymentMethod {
  return paymentMethods.includes(value as PaymentMethod)
}

function getProductSearchText(product: ProductResponse) {
  return [product.name, product.description, product.categoryName, product.id].join(" ").toLowerCase()
}

function compareProducts(first: ProductResponse, second: ProductResponse) {
  return first.name.localeCompare(second.name) || first.id - second.id
}

export function SagaDemoPage() {
  const navigate = useNavigate()
  const { customers, isRefreshing: isRefreshingCustomers, refresh: refreshCustomers } = useCustomersData()
  const { products, isRefreshing: isRefreshingProducts, refresh: refreshProducts } = useProductsData()
  const [draft, setDraft] = useState<SagaDemoDraft>(() => readStoredDraft())
  const [customerSearch, setCustomerSearch] = useState("")
  const [productSearch, setProductSearch] = useState("")
  const [category, setCategory] = useState("ALL")
  const [refreshWarnings, setRefreshWarnings] = useState<string[]>([])
  const [createError, setCreateError] = useState<string | null>(null)
  const [isCreating, setIsCreating] = useState(false)

  useEffect(() => {
    sessionStorage.setItem(draftStorageKey, JSON.stringify(draft))
  }, [draft])

  useEffect(() => {
    let active = true

    queueMicrotask(() => {
      if (!active) {
        return
      }

      let nextWarnings: string[] = []

      setDraft((current) => {
        const warnings: string[] = []
        let next = current

        if (customers.status === "success" && current.selectedCustomerId) {
          const customerExists = customers.data.some((customer) => customer.id === current.selectedCustomerId)

          if (!customerExists) {
            warnings.push("Selected customer disappeared after refresh and was removed from the draft.")
            next = { ...next, selectedCustomerId: null, step: Math.min(next.step, 0) }
          }
        }

        if (products.status === "success") {
          const productIds = new Set(products.data.map((product) => String(product.id)))
          const keptQuantities = Object.fromEntries(Object.entries(next.productQuantities).filter(([productId]) => productIds.has(productId)))

          if (Object.keys(keptQuantities).length !== Object.keys(next.productQuantities).length) {
            warnings.push("One or more selected products disappeared after refresh and were removed from the draft.")
            next = { ...next, productQuantities: keptQuantities, step: Math.min(next.step, 1) }
          }
        }

        nextWarnings = warnings
        return next
      })
      setRefreshWarnings(nextWarnings)
    })

    return () => {
      active = false
    }
  }, [customers.data, customers.status, products.data, products.status])

  const selectedCustomer = customers.data.find((customer) => customer.id === draft.selectedCustomerId) ?? null

  const visibleCustomers = useMemo(() => {
    const normalizedSearch = customerSearch.trim().toLowerCase()
    const sortedCustomers = [...customers.data].sort(compareCustomers)

    if (!normalizedSearch) {
      return sortedCustomers
    }

    return sortedCustomers.filter((customer) => getCustomerSearchText(customer).includes(normalizedSearch))
  }, [customerSearch, customers.data])

  const categories = useMemo(
    () => [...new Set(products.data.map((product) => product.categoryName))].sort((first, second) => first.localeCompare(second)),
    [products.data]
  )

  const visibleProducts = useMemo(() => {
    const normalizedSearch = productSearch.trim().toLowerCase()

    return [...products.data]
      .sort(compareProducts)
      .filter((product) => (category === "ALL" ? true : product.categoryName === category))
      .filter((product) => (normalizedSearch ? getProductSearchText(product).includes(normalizedSearch) : true))
  }, [category, productSearch, products.data])

  const selectedLines = useMemo<SelectedLine[]>(() => {
    return products.data
      .map((product) => {
        const quantity = draft.productQuantities[String(product.id)] ?? 0

        return quantity > 0 ? { product, quantity, subtotal: product.price * quantity } : null
      })
      .filter((line): line is SelectedLine => Boolean(line))
      .sort((first, second) => compareProducts(first.product, second.product))
  }, [draft.productQuantities, products.data])

  const estimatedTotal = selectedLines.reduce((total, line) => total + line.subtotal, 0)
  const overStockLines = selectedLines.filter((line) => line.quantity > line.product.availableQuantity)
  const warningMessages = [
    ...refreshWarnings,
    ...overStockLines.map(
      (line) =>
        `${line.product.name}: requested ${line.quantity}, available ${line.product.availableQuantity}. Demo mode may drive PRODUCT_RESERVATION_FAILED.`
    ),
  ]
  const hasCustomer = Boolean(selectedCustomer)
  const hasProducts = selectedLines.length > 0
  const hasPayment = Boolean(draft.paymentMethod)
  const canAdvance =
    (draft.step === 0 && hasCustomer) ||
    (draft.step === 1 && hasProducts) ||
    (draft.step === 2 && hasPayment) ||
    draft.step === 3

  function updateDraft(update: (current: SagaDemoDraft) => SagaDemoDraft) {
    setCreateError(null)
    setDraft((current) => update(current))
  }

  function setStep(step: number) {
    updateDraft((current) => ({ ...current, step: clampStep(step) }))
  }

  function setQuantity(productId: number, value: unknown) {
    const quantity = clampQuantity(value)

    updateDraft((current) => {
      const productQuantities = { ...current.productQuantities }

      if (quantity > 0) {
        productQuantities[String(productId)] = quantity
      } else {
        delete productQuantities[String(productId)]
      }

      return { ...current, productQuantities }
    })
  }

  function resetDemo() {
    const nextDraft = createInitialDraft()
    sessionStorage.setItem(draftStorageKey, JSON.stringify(nextDraft))
    setDraft(nextDraft)
    setCreateError(null)
    setRefreshWarnings([])
  }

  async function submitCreateOrder() {
    if (!selectedCustomer || !draft.paymentMethod || selectedLines.length === 0) {
      return
    }

    setIsCreating(true)
    setCreateError(null)

    try {
      const response = await createOrder({
        reference: draft.reference,
        customerId: selectedCustomer.id,
        paymentMethod: draft.paymentMethod,
        products: selectedLines.map((line) => ({ productId: line.product.id, quantity: line.quantity })),
      })

      sessionStorage.removeItem(draftStorageKey)
      navigate(`/orders/${response.orderId}/flow`)
    } catch (caught) {
      setCreateError(formatOrderApiError(caught, "Order creation failed"))
    } finally {
      setIsCreating(false)
    }
  }

  return (
    <AppShell>
      <div className="flex flex-col gap-6">
        <PageHeader
          eyebrow="Saga Demo"
          title="Start Saga Demo"
          description="Create a demo order, then follow product reservation and payment resolution in Order Flow."
          badge={<Badge variant="outline">Restricted: Admin</Badge>}
          actions={
            <Button type="button" variant="outline" onClick={resetDemo}>
              Reset demo
            </Button>
          }
        />

        <section className="grid gap-4 lg:grid-cols-[1fr_0.34fr] lg:items-start">
          <div className="flex flex-col gap-4">
            <StepNavigation
              currentStep={draft.step}
              canVisitStep={(step) => canVisitStep(step, hasCustomer, hasProducts, hasPayment)}
              onStepChange={setStep}
            />

            {draft.step === 0 ? (
              <CustomerStep
                actions={
                  <StepActions
                    currentStep={draft.step}
                    canAdvance={canAdvance}
                    isCreating={isCreating}
                    onBack={() => setStep(draft.step - 1)}
                    onNext={() => setStep(draft.step + 1)}
                  />
                }
                customers={customers}
                isRefreshing={isRefreshingCustomers}
                search={customerSearch}
                selectedCustomerId={draft.selectedCustomerId}
                visibleCustomers={visibleCustomers}
                onRefresh={() => void refreshCustomers()}
                onSearchChange={setCustomerSearch}
                onSelectCustomer={(customer) => updateDraft((current) => ({ ...current, selectedCustomerId: customer.id }))}
              />
            ) : null}

            {draft.step === 1 ? (
              <ProductStep
                actions={
                  <StepActions
                    currentStep={draft.step}
                    canAdvance={canAdvance}
                    isCreating={isCreating}
                    onBack={() => setStep(draft.step - 1)}
                    onNext={() => setStep(draft.step + 1)}
                  />
                }
                category={category}
                categories={categories}
                isRefreshing={isRefreshingProducts}
                products={products}
                productSearch={productSearch}
                quantities={draft.productQuantities}
                visibleProducts={visibleProducts}
                onCategoryChange={setCategory}
                onProductSearchChange={setProductSearch}
                onQuantityChange={setQuantity}
                onRefresh={() => void refreshProducts()}
              />
            ) : null}

            {draft.step === 2 ? (
              <PaymentStep
                actions={
                  <StepActions
                    currentStep={draft.step}
                    canAdvance={canAdvance}
                    isCreating={isCreating}
                    onBack={() => setStep(draft.step - 1)}
                    onNext={() => setStep(draft.step + 1)}
                  />
                }
                paymentMethod={draft.paymentMethod}
                onPaymentMethodChange={(paymentMethod) => updateDraft((current) => ({ ...current, paymentMethod }))}
              />
            ) : null}

            {draft.step === 3 ? (
              <ReviewStep
                actions={
                  <StepActions
                    currentStep={draft.step}
                    canAdvance={canAdvance}
                    isCreating={isCreating}
                    onBack={() => setStep(draft.step - 1)}
                    onNext={() => undefined}
                  />
                }
                createError={createError}
                draft={draft}
                estimatedTotal={estimatedTotal}
                isCreating={isCreating}
                selectedCustomer={selectedCustomer}
                selectedLines={selectedLines}
                warningMessages={warningMessages}
                onSubmit={() => void submitCreateOrder()}
              />
            ) : null}

          </div>

          <aside className="flex flex-col gap-4 lg:sticky lg:top-24">
            <DraftSummary
              draft={draft}
              estimatedTotal={estimatedTotal}
              selectedCustomer={selectedCustomer}
              selectedLines={selectedLines}
              warningCount={warningMessages.length}
            />
          </aside>
        </section>
      </div>
    </AppShell>
  )
}

function canVisitStep(step: number, hasCustomer: boolean, hasProducts: boolean, hasPayment: boolean) {
  if (step === 0) {
    return true
  }

  if (step === 1) {
    return hasCustomer
  }

  if (step === 2) {
    return hasCustomer && hasProducts
  }

  return hasCustomer && hasProducts && hasPayment
}

function StepNavigation({
  currentStep,
  canVisitStep,
  onStepChange,
}: {
  currentStep: number
  canVisitStep: (step: number) => boolean
  onStepChange: (step: number) => void
}) {
  return (
    <Card size="sm">
      <CardHeader>
        <CardTitle>Wizard path</CardTitle>
        <CardDescription>Forward movement is gated by valid customer, product, and payment choices.</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="grid gap-2 md:grid-cols-4">
          {steps.map((step, index) => {
            const isCurrent = index === currentStep
            const canVisit = canVisitStep(index)

            return (
              <Button
                key={step}
                type="button"
                variant={isCurrent ? "default" : "outline"}
                disabled={!canVisit}
                onClick={() => onStepChange(index)}
              >
                <span className="font-mono">0{index + 1}</span>
                {step}
              </Button>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}

function StepActions({
  currentStep,
  canAdvance,
  isCreating,
  onBack,
  onNext,
}: {
  currentStep: number
  canAdvance: boolean
  isCreating: boolean
  onBack: () => void
  onNext: () => void
}) {
  return (
    <div className="flex flex-wrap justify-end gap-2">
      <Button type="button" variant="outline" disabled={currentStep === 0 || isCreating} onClick={onBack}>
        Back
      </Button>
      {currentStep < steps.length - 1 ? (
        <Button type="button" disabled={!canAdvance || isCreating} onClick={onNext}>
          Next
        </Button>
      ) : null}
    </div>
  )
}

function CustomerStep({
  actions,
  customers,
  isRefreshing,
  search,
  selectedCustomerId,
  visibleCustomers,
  onRefresh,
  onSearchChange,
  onSelectCustomer,
}: {
  actions: React.ReactNode
  customers: ReturnType<typeof useCustomersData>["customers"]
  isRefreshing: boolean
  search: string
  selectedCustomerId: string | null
  visibleCustomers: CustomerResponse[]
  onRefresh: () => void
  onSearchChange: (value: string) => void
  onSelectCustomer: (customer: CustomerResponse) => void
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Select Customer</CardTitle>
        <CardDescription>Choose an existing customer for Order Service validation.</CardDescription>
        <CardAction>
          <div className="flex flex-wrap justify-end gap-2">
            <Button type="button" variant="outline" onClick={onRefresh} disabled={isRefreshing}>
              {isRefreshing ? "Refreshing" : "Refresh"}
            </Button>
            {actions}
          </div>
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="saga-demo-customer-search">Search customers</FieldLabel>
            <Input
              id="saga-demo-customer-search"
              value={search}
              placeholder="Search name, email, or customer ID"
              onChange={(event) => onSearchChange(event.target.value)}
            />
            <FieldDescription>Uses loaded customer records only.</FieldDescription>
          </Field>
        </FieldGroup>

        {customers.status === "loading" ? <TableSkeleton columns={3} rows={5} /> : null}
        {customers.status === "error" ? <ErrorState title="Customer list failed" message={customers.error ?? "Customer request failed"} /> : null}
        {customers.status === "success" && customers.data.length === 0 ? (
          <EmptyState
            title="No customers available."
            description={
              <span>
                Create a customer on <Link to="/customers">Customers</Link>, then refresh this step.
              </span>
            }
          />
        ) : null}
        {customers.status === "success" && customers.data.length > 0 && visibleCustomers.length === 0 ? (
          <EmptyState title="No customers match this search." description="Reset search or refresh customer records." />
        ) : null}
        {visibleCustomers.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Customer ID</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {visibleCustomers.map((customer) => {
                const isSelected = customer.id === selectedCustomerId

                return (
                  <TableRow
                    key={customer.id}
                    aria-selected={isSelected}
                    data-state={isSelected ? "selected" : undefined}
                    tabIndex={0}
                    className="cursor-pointer"
                    onClick={() => onSelectCustomer(customer)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter" || event.key === " ") {
                        event.preventDefault()
                        onSelectCustomer(customer)
                      }
                    }}
                  >
                    <TableCell className="font-medium">{formatCustomerName(customer)}</TableCell>
                    <TableCell>{customer.email}</TableCell>
                    <TableCell className="font-mono text-xs">{customer.id}</TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        ) : null}
      </CardContent>
    </Card>
  )
}

function ProductStep({
  actions,
  category,
  categories,
  isRefreshing,
  products,
  productSearch,
  quantities,
  visibleProducts,
  onCategoryChange,
  onProductSearchChange,
  onQuantityChange,
  onRefresh,
}: {
  actions: React.ReactNode
  category: string
  categories: string[]
  isRefreshing: boolean
  products: ReturnType<typeof useProductsData>["products"]
  productSearch: string
  quantities: Record<string, number>
  visibleProducts: ProductResponse[]
  onCategoryChange: (value: string) => void
  onProductSearchChange: (value: string) => void
  onQuantityChange: (productId: number, value: unknown) => void
  onRefresh: () => void
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Select Products</CardTitle>
        <CardDescription>Set quantity to select products. Above-stock quantities are allowed for failure-path demos.</CardDescription>
        <CardAction>
          <div className="flex flex-wrap justify-end gap-2">
            <Button type="button" variant="outline" onClick={onRefresh} disabled={isRefreshing}>
              {isRefreshing ? "Refreshing" : "Refresh"}
            </Button>
            {actions}
          </div>
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <FieldGroup className="md:grid md:grid-cols-[1.4fr_0.8fr] md:gap-3">
          <Field>
            <FieldLabel htmlFor="saga-demo-product-search">Search products</FieldLabel>
            <Input
              id="saga-demo-product-search"
              value={productSearch}
              placeholder="Search name, description, category, or ID"
              onChange={(event) => onProductSearchChange(event.target.value)}
            />
          </Field>
          <Field>
            <FieldLabel htmlFor="saga-demo-category">Category</FieldLabel>
            <select
              id="saga-demo-category"
              className="h-8 rounded-lg border border-input bg-background px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
              value={category}
              onChange={(event) => onCategoryChange(event.target.value)}
            >
              <option value="ALL">All categories</option>
              {categories.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </Field>
        </FieldGroup>

        {products.status === "loading" ? <TableSkeleton columns={7} rows={5} /> : null}
        {products.status === "error" ? <ErrorState title="Product list failed" message={products.error ?? "Product request failed"} /> : null}
        {products.status === "success" && products.data.length === 0 ? (
          <EmptyState title="No products available." description="Create products in Catalog before running the saga demo." />
        ) : null}
        {products.status === "success" && products.data.length > 0 && visibleProducts.length === 0 ? (
          <EmptyState title="No products match these filters." description="Reset search/category or refresh product records." />
        ) : null}
        {visibleProducts.length > 0 ? (
          <ScrollArea className="h-[min(48dvh,30rem)] rounded-xl border">
            <Table>
              <TableHeader className="sticky top-0 z-10 bg-background">
                <TableRow>
                  <TableHead className="bg-background">Product</TableHead>
                  <TableHead className="bg-background">Category</TableHead>
                  <TableHead className="bg-background">Stock</TableHead>
                  <TableHead className="bg-background">Available</TableHead>
                  <TableHead className="bg-background">Unit price</TableHead>
                  <TableHead className="bg-background">Quantity</TableHead>
                  <TableHead className="bg-background">Subtotal</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {visibleProducts.map((product) => {
                  const quantity = quantities[String(product.id)] ?? 0
                  const overStock = quantity > product.availableQuantity

                  return (
                    <TableRow key={product.id} data-state={quantity > 0 ? "selected" : undefined}>
                      <TableCell>
                        <div className="flex flex-col gap-1">
                          <span className="font-medium">{product.name}</span>
                          <span className="font-mono text-xs text-muted-foreground">#{product.id}</span>
                        </div>
                      </TableCell>
                      <TableCell>{product.categoryName}</TableCell>
                      <TableCell>
                        <ProductStockBadge availableQuantity={product.availableQuantity} />
                      </TableCell>
                      <TableCell className="font-mono">{product.availableQuantity}</TableCell>
                      <TableCell>{formatMoney(product.price)}</TableCell>
                      <TableCell>
                        <div className="flex min-w-32 flex-col gap-2">
                          <Input
                            aria-label={`Quantity for ${product.name}`}
                            min={0}
                            max={maxQuantity}
                            step={1}
                            type="number"
                            value={quantity}
                            onChange={(event) => onQuantityChange(product.id, event.target.value)}
                          />
                          {overStock ? <Badge variant="destructive">Reservation failure demo</Badge> : null}
                        </div>
                      </TableCell>
                      <TableCell>{quantity > 0 ? formatMoney(product.price * quantity) : "not selected"}</TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </ScrollArea>
        ) : null}
      </CardContent>
    </Card>
  )
}

function PaymentStep({
  actions,
  paymentMethod,
  onPaymentMethodChange,
}: {
  actions: React.ReactNode
  paymentMethod: PaymentMethod | null
  onPaymentMethodChange: (paymentMethod: PaymentMethod) => void
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Select Payment</CardTitle>
        <CardDescription>Pick the payment method sent to Order Service. No default is selected.</CardDescription>
        <CardAction>{actions}</CardAction>
      </CardHeader>
      <CardContent>
        <FieldSet>
          <FieldLegend>Payment method</FieldLegend>
          <FieldDescription>Friendly label plus raw enum value stays visible for backend review.</FieldDescription>
          <ToggleGroup
            type="single"
            variant="outline"
            value={paymentMethod ?? ""}
            className="grid w-full gap-3 md:grid-cols-2 xl:grid-cols-3"
            onValueChange={(value) => {
              if (isPaymentMethod(value)) {
                onPaymentMethodChange(value)
              }
            }}
          >
            {paymentMethods.map((method) => (
              <ToggleGroupItem key={method} value={method} className="h-auto flex-col items-start p-3 text-left">
                <span className="font-medium">{formatPaymentMethod(method)}</span>
                <span className="font-mono text-xs text-muted-foreground">{method}</span>
              </ToggleGroupItem>
            ))}
          </ToggleGroup>
        </FieldSet>
      </CardContent>
    </Card>
  )
}

function ReviewStep({
  actions,
  createError,
  draft,
  estimatedTotal,
  isCreating,
  selectedCustomer,
  selectedLines,
  warningMessages,
  onSubmit,
}: {
  actions: React.ReactNode
  createError: string | null
  draft: SagaDemoDraft
  estimatedTotal: number
  isCreating: boolean
  selectedCustomer: CustomerResponse | null
  selectedLines: SelectedLine[]
  warningMessages: string[]
  onSubmit: () => void
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Review And Create</CardTitle>
        <CardDescription>Confirm the request payload before Order Service starts the saga.</CardDescription>
        <CardAction>{actions}</CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <section className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          <DetailItem label="Reference" value={draft.reference} mono />
          <DetailItem label="Customer" value={selectedCustomer ? formatCustomerName(selectedCustomer) : "not selected"} />
          <DetailItem label="Customer ID" value={selectedCustomer?.id ?? "not selected"} mono />
          <DetailItem label="Payment" value={draft.paymentMethod ? formatPaymentMethod(draft.paymentMethod) : "not selected"} />
          <DetailItem label="Payment enum" value={draft.paymentMethod ?? "not selected"} mono />
          <DetailItem label="Estimated total" value={formatMoney(estimatedTotal)} />
        </section>

        <Card size="sm">
          <CardHeader>
            <CardTitle>Products</CardTitle>
            <CardDescription>Selected product IDs, requested quantities, and current list prices.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Product</TableHead>
                  <TableHead>Product ID</TableHead>
                  <TableHead>Quantity</TableHead>
                  <TableHead>Unit price</TableHead>
                  <TableHead>Estimated subtotal</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {selectedLines.map((line) => (
                  <TableRow key={line.product.id}>
                    <TableCell className="font-medium">{line.product.name}</TableCell>
                    <TableCell className="font-mono">{line.product.id}</TableCell>
                    <TableCell className="font-mono">{line.quantity}</TableCell>
                    <TableCell>{formatMoney(line.product.price)}</TableCell>
                    <TableCell>{formatMoney(line.subtotal)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        {warningMessages.length > 0 ? (
          <Alert>
            <AlertTitle>Warnings</AlertTitle>
            <AlertDescription>
              <ul className="flex list-disc flex-col gap-1 pl-4">
                {warningMessages.map((warning) => (
                  <li key={warning}>{warning}</li>
                ))}
              </ul>
            </AlertDescription>
          </Alert>
        ) : null}

        {createError ? <ErrorState title="Order creation failed" message={createError} /> : null}
      </CardContent>
      <CardFooter className="justify-end">
        <Button type="button" disabled={isCreating} onClick={onSubmit}>
          {isCreating ? "Creating" : "Create Order and View Flow"}
        </Button>
      </CardFooter>
    </Card>
  )
}

function DraftSummary({
  draft,
  estimatedTotal,
  selectedCustomer,
  selectedLines,
  warningCount,
}: {
  draft: SagaDemoDraft
  estimatedTotal: number
  selectedCustomer: CustomerResponse | null
  selectedLines: SelectedLine[]
  warningCount: number
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Draft summary</CardTitle>
        <CardDescription>Session-scoped state reused for retries.</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-3">
        <DetailItem label="Reference" value={draft.reference} mono />
        <DetailItem label="Customer" value={selectedCustomer ? formatCustomerName(selectedCustomer) : "not selected"} />
        <DetailItem label="Products" value={`${selectedLines.length} selected`} mono />
        <DetailItem label="Estimated total" value={formatMoney(estimatedTotal)} />
        <DetailItem label="Payment" value={draft.paymentMethod ? formatPaymentMethod(draft.paymentMethod) : "not selected"} />
        <div className="flex items-center justify-between rounded-xl border p-3">
          <span className="text-xs text-muted-foreground">Warnings</span>
          <Badge variant={warningCount > 0 ? "destructive" : "secondary"}>{warningCount}</Badge>
        </div>
      </CardContent>
    </Card>
  )
}
