type RequestBody = BodyInit | null | undefined

export class ApiClientError extends Error {
  status: number
  statusText: string
  body: unknown

  constructor(status: number, statusText: string, body: unknown) {
    super(`Request failed with ${status} ${statusText}`)
    this.name = "ApiClientError"
    this.status = status
    this.statusText = statusText
    this.body = body
  }
}

async function parseResponse(response: Response) {
  const text = await response.text()

  if (!text) {
    return undefined
  }

  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

export async function apiFetch<T>(input: RequestInfo | URL, init: RequestInit = {}) {
  const method = (init.method ?? "GET").toUpperCase()
  const headers = new Headers(init.headers)
  const body = init.body as RequestBody

  if (body && !(body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json")
  }

  const response = await fetch(input, {
    ...init,
    method,
    headers,
    credentials: "include",
  })
  const responseBody = await parseResponse(response)

  if (!response.ok) {
    throw new ApiClientError(response.status, response.statusText, responseBody)
  }

  return responseBody as T
}
