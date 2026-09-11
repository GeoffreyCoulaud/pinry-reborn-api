import { screen } from "@testing-library/react"
import { describe, expect, it } from "vitest"
import { m } from "../paraglide/messages.js"
import { downloadsRoute, pinsRoute, renderApp, sessionRoute } from "../test/app"
import { server } from "../test/server"

describe("open the application", () => {
  it("Given an open session, Then the home heading is on screen", async () => {
    server.use(sessionRoute(() => true), pinsRoute([]), downloadsRoute())

    renderApp("/")

    expect(await screen.findByRole("heading", { name: m.home_heading() })).toBeVisible()
  })
})
