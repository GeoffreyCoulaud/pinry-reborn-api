import type { Schemas } from "@pinry-reborn/auth"
import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { RouterProvider, createMemoryHistory } from "@tanstack/react-router"
import { render } from "@testing-library/react"
import { HttpResponse, http } from "msw"
import { createAppRouter } from "../router"

type Pin = Schemas["PinOutputDto"]

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

const AUTHOR_ID = "0f5c6e58-2d6c-4a3a-9c1f-2a1f6b6d4f11"
let pinCount = 0

/** A pin the journey names by its description, which the tile reads as the image's text. */
export function pin(description: string, image: Pin["image"]): Pin {
  const id = `${AUTHOR_ID.slice(0, -2)}${(pinCount++).toString().padStart(2, "0")}`
  return {
    id,
    authorId: AUTHOR_ID,
    sourceContextUrl: `https://example.test/${id}`,
    sourceMediaUrl: null,
    description,
    tags: [],
    boards: [],
    image: image && { ...image, url: `/api/v1/pins/${id}/image` },
  }
}

/** A pin whose image the API downloaded, at the dimensions the tile is placed with. */
export function readyPin(description: string, width = 800, height = 600): Pin {
  return pin(description, { status: "READY", url: "", width, height })
}

/**
 * The catalogue the grid pages through. A cursor is the index of the page it answers, which is
 * all the client may assume of it: block 5 made it an opaque string.
 */
export function pinsRoute(pages: Pin[][]) {
  return http.get("/api/v1/pins", ({ request }) => {
    const cursor = new URL(request.url).searchParams.get("cursor")
    const index = cursor === null ? 0 : Number(cursor)
    return HttpResponse.json({
      pins: pages[index] ?? [],
      pagination: {
        previousCursor: index > 0 ? String(index - 1) : null,
        nextCursor: index + 1 < pages.length ? String(index + 1) : null,
      },
    })
  })
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
