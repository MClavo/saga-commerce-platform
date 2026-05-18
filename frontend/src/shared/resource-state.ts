export type ResourceState<T> =
  | { status: "loading"; data: T[]; error: null }
  | { status: "success"; data: T[]; error: null }
  | { status: "error"; data: T[]; error: string }
  | { status: "restricted"; data: T[]; error: null; roles: string[] }

export function loadingState<T>(): ResourceState<T> {
  return { status: "loading", data: [], error: null }
}

export function restrictedState<T>(roles: string[]): ResourceState<T> {
  return { status: "restricted", data: [], error: null, roles }
}

export async function loadResource<T>(loader: () => Promise<T[]>, fallback = "Request failed"): Promise<ResourceState<T>> {
  try {
    return {
      status: "success",
      data: await loader(),
      error: null,
    }
  } catch (caught) {
    return {
      status: "error",
      data: [],
      error: caught instanceof Error ? caught.message : fallback,
    }
  }
}
