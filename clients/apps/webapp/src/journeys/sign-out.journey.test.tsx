import { screen } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { HttpResponse, http } from "msw"
import { describe, expect, it } from "vitest"
import { m } from "../paraglide/messages.js"
import { downloadsRoute, pinsRoute, renderApp, sessionRoute } from "../test/app"
import { server } from "../test/server"

describe("sign out", () => {
  it("Given an open session, Then signing out asks for the credentials again", async () => {
    let open = true
    server.use(
      sessionRoute(() => open),
      pinsRoute([]),
      downloadsRoute(),
      http.delete("/api/v1/sessions/current", () => {
        open = false
        return new HttpResponse(null, { status: 204 })
      }),
    )
    renderApp("/")
    const user = userEvent.setup()

    await user.click(await screen.findByRole("button", { name: m.sign_out() }))

    expect(await screen.findByRole("heading", { name: m.sign_in() })).toBeVisible()
    expect(open).toBe(false)
  })
})
