import { screen, waitFor } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { HttpResponse, http } from "msw"
import { describe, expect, it } from "vitest"
import { download, downloadsRoute, onePinPage, pin, renderApp, sessionRoute } from "../test/app"
import { server } from "../test/server"

describe("a failed download surfacing in the task centre", () => {
  it("Given a download that failed, Then the centre says why and offers the recourse", async () => {
    const user = userEvent.setup()
    const failed = pin("a cat asleep", {
      status: "FAILED",
      reasonCode: "NOT_FOUND",
      message: "No image at this URL.",
    })
    let retried: string | null = null
    server.use(
      sessionRoute(() => true),
      onePinPage(() => [failed]),
      downloadsRoute(() => [download(failed.id, "FAILED", "The server could not fetch it.")]),
      http.put("/api/v1/pins/:pinId/image", ({ params }) => {
        retried = String(params.pinId)
        return HttpResponse.json({ status: "PENDING" }, { status: 202 })
      }),
    )

    renderApp("/")
    await user.click(await screen.findByRole("button", { name: "Downloads (1)" }))

    expect(await screen.findByText("Failed")).toBeVisible()
    expect(screen.getByText("The server could not fetch it.")).toBeVisible()
    // The recourse question V exists for: the address again, a file from disk, or neither.
    expect(screen.getByLabelText("Image file")).toBeInTheDocument()
    expect(screen.getByRole("button", { name: "Forget it" })).toBeVisible()

    await user.click(screen.getByRole("button", { name: "Try again" }))

    await waitFor(() => expect(retried).toBe(failed.id))
  })
})
