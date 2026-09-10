import { screen } from "@testing-library/react"
import { describe, expect, it } from "vitest"
import { pin, pinsRoute, readyPin, renderApp, sessionRoute } from "../test/app"
import { server } from "../test/server"

describe("browse the grid and load a second page", () => {
  it("Given a catalogue of two pages, Then the end of the first brings the second", async () => {
    const first = readyPin("a harbour at dusk")
    const second = readyPin("a cat asleep")
    server.use(sessionRoute(() => true), pinsRoute([[first], [second]]))

    renderApp("/")

    expect(await screen.findByRole("img", { name: first.description })).toBeVisible()
    expect(await screen.findByRole("img", { name: second.description })).toBeVisible()
  })

  it("Given an image the API measured, Then the tile is placed at that ratio before it loads", async () => {
    const wide = readyPin("a harbour at dusk", 800, 600)
    server.use(sessionRoute(() => true), pinsRoute([[wide]]))

    renderApp("/")

    const tile = await screen.findByRole("img", { name: wide.description })
    expect(tile).toHaveStyle({ aspectRatio: "800 / 600" })
    // The column is unmeasurable in jsdom, so the narrowest rendition is what a zero width asks for.
    expect(tile).toHaveAttribute("src", `/api/v1/pins/${wide.id}/image?size=SMALL`)
  })

  it("Given a download the server is still running, Then no tile stands for the pin", async () => {
    const ready = readyPin("a harbour at dusk")
    const pending = pin("a cat asleep", { status: "PENDING" })
    server.use(sessionRoute(() => true), pinsRoute([[ready, pending]]))

    renderApp("/")

    expect(await screen.findByRole("img", { name: ready.description })).toBeVisible()
    expect(screen.queryByText(pending.description)).toBeNull()
  })

  it("Given a download that failed, Then its tile carries the reason the API gave", async () => {
    const failed = pin("a cat asleep", {
      status: "FAILED",
      reasonCode: "NOT_FOUND",
      message: "No image at this URL.",
    })
    server.use(sessionRoute(() => true), pinsRoute([[failed]]))

    renderApp("/")

    expect(await screen.findByText("No image at this URL.")).toBeVisible()
  })
})
