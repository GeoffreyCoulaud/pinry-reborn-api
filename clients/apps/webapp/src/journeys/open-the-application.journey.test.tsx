import { RouterProvider, createMemoryHistory } from "@tanstack/react-router"
import { render, screen } from "@testing-library/react"
import { describe, expect, it } from "vitest"
import { m } from "../paraglide/messages.js"
import { createAppRouter } from "../router"

describe("open the application", () => {
  it("Given the application's only route, Then the home heading is on screen", async () => {
    const router = createAppRouter(createMemoryHistory({ initialEntries: ["/"] }))

    render(<RouterProvider router={router} />)

    expect(await screen.findByRole("heading", { name: m.home_heading() })).toBeVisible()
  })
})
