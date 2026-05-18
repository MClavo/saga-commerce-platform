import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"

type CustomerFiltersProps = {
  search: string
  onSearchChange: (value: string) => void
  onReset: () => void
}

export function CustomerFilters({ search, onSearchChange, onReset }: CustomerFiltersProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Customer search</CardTitle>
        <CardDescription>Local search across name, email, customer ID, and address.</CardDescription>
        <CardAction>
          <Button type="button" variant="outline" onClick={onReset} disabled={!search.trim()}>
            Reset
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent>
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="customer-search">Search customers</FieldLabel>
            <Input
              id="customer-search"
              value={search}
              placeholder="Search by name, email, ID, or address"
              onChange={(event) => onSearchChange(event.target.value)}
            />
            <FieldDescription>Uses loaded customer records only; no server-side query.</FieldDescription>
          </Field>
        </FieldGroup>
      </CardContent>
    </Card>
  )
}
