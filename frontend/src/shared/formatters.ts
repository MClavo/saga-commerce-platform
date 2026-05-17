export const euroFormatter = new Intl.NumberFormat("de-DE", {
  style: "currency",
  currency: "EUR",
})

export const compactDateTimeFormatter = new Intl.DateTimeFormat("en-GB", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
})

export function formatMoney(value: number | null | undefined) {
  if (typeof value !== "number" || Number.isNaN(value)) {
    return "not set"
  }

  return euroFormatter.format(value)
}

export function formatCompactDateTime(value: string | null | undefined) {
  if (!value) {
    return "not recorded"
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return "invalid date"
  }

  return compactDateTimeFormatter.format(date)
}

export function compareByNewestDateThenId(
  first: { createdAt?: string | null; id?: number | null },
  second: { createdAt?: string | null; id?: number | null }
) {
  const firstTime = first.createdAt ? new Date(first.createdAt).getTime() : Number.NaN
  const secondTime = second.createdAt ? new Date(second.createdAt).getTime() : Number.NaN
  const firstValid = !Number.isNaN(firstTime)
  const secondValid = !Number.isNaN(secondTime)

  if (firstValid && secondValid && firstTime !== secondTime) {
    return secondTime - firstTime
  }

  if (firstValid !== secondValid) {
    return firstValid ? -1 : 1
  }

  return (second.id ?? 0) - (first.id ?? 0)
}
