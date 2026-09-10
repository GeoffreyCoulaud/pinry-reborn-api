import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { RouterProvider, createMemoryHistory } from "@tanstack/react-router"
import { render } from "@testing-library/react"
import { HttpResponse, http } from "msw"
import { createAppRouter } from "../router"

/** What both transports agree on, which is all the application ever reads of a session. */
export const SESSION = {
  expiresAt: "2026-09-11T06:00:00Z",
  renewAfter: "2026-09-10T22:00:00Z",
  persistent: false,
}

/** The route the application reads its session from, answered from what the journey decided last. */
export function sessionRoute(isOpen: () => boolean) {
  return http.get("/api/v1/sessions/current", () =>
    isOpen() ? HttpResponse.json(SESSION) : new HttpResponse(null, { status: 401 }),
  )
}

/** The application on one route, with a cache of its own so no journey inherits another's. */
export function renderApp(path: string) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={createAppRouter(createMemoryHistory({ initialEntries: [path] }))} />
    </QueryClientProvider>,
  )
}
