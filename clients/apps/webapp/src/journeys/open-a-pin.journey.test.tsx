import { screen, within } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { describe, expect, it } from "vitest"
import { m } from "../paraglide/messages.js"
import { downloadsRoute, pinsRoute, readyPin, renderApp, sessionRoute } from "../test/app"
import { server } from "../test/server"

describe("open a pin", () => {
  it("Given a tile, Then the pin opens with what the API knows of it", async () => {
    const opened = {
      ...readyPin("a harbour at dusk"),
      tags: [{ name: "harbours" }],
      boards: [{ id: "5e0d2a52-6f7c-4f5e-9c2a-8b3e0d1a7c44", name: "Evenings" }],
    }
    server.use(sessionRoute(() => true), pinsRoute([[opened]]), downloadsRoute())
    renderApp("/")
    const user = userEvent.setup()

    await user.click(await screen.findByRole("img", { name: opened.description }))

    const dialog = await screen.findByRole("dialog")
    expect(within(dialog).getByRole("img", { name: opened.description })).toBeVisible()
    expect(within(dialog).getByText("harbours")).toBeVisible()
    expect(within(dialog).getByText("Evenings")).toBeVisible()
  })

  it("Given an open pin, Then closing it returns to the grid", async () => {
    const opened = readyPin("a harbour at dusk")
    server.use(sessionRoute(() => true), pinsRoute([[opened]]), downloadsRoute())
    renderApp("/")
    const user = userEvent.setup()

    await user.click(await screen.findByRole("img", { name: opened.description }))
    await user.click(await screen.findByRole("button", { name: m.close() }))

    expect(screen.queryByRole("dialog")).toBeNull()
  })
})
