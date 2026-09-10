import { screen } from "@testing-library/react"
import { describe, expect, it } from "vitest"
import { m } from "../paraglide/messages.js"
import { renderApp, sessionRoute } from "../test/app"
import { server } from "../test/server"

describe("session expiry", () => {
  it("Given a session the API no longer honours, Then the pins are never shown", async () => {
    server.use(sessionRoute(() => false))
    renderApp("/")

    expect(await screen.findByRole("heading", { name: m.sign_in() })).toBeVisible()
    expect(screen.queryByRole("heading", { name: m.home_heading() })).toBeNull()
  })
})
