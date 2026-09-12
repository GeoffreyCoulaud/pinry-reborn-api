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

  it("Given a file uploaded from the centre, Then the grid rereads and the tile appears", async () => {
    const user = userEvent.setup()
    const failed = pin("a cat asleep", { status: "FAILED", reasonCode: "FETCH_FAILED" })
    const url = `/api/v1/pins/${failed.id}/image`
    const ready = { ...failed, image: { status: "READY" as const, url, width: 800, height: 600 } }
    let stored = false
    server.use(
      sessionRoute(() => true),
      onePinPage(() => [stored ? ready : failed]),
      // The upload clears the row on the server, so the list empties with it and a DELETE the
      // client sent afterwards would answer 404 (SetPinImage calls ClearPinDownload).
      downloadsRoute(() => (stored ? [] : [download(failed.id, "FAILED")])),
      http.put("/api/v1/pins/:pinId/image", () => {
        stored = true
        return HttpResponse.json({ id: failed.id, pinId: failed.id }, { status: 200 })
      }),
      http.delete("/api/v1/me/image-downloads/:pinId", () => new HttpResponse(null, { status: 404 })),
    )

    renderApp("/")
    await user.click(await screen.findByRole("button", { name: "Downloads (1)" }))
    await user.upload(
      screen.getByLabelText("Image file"),
      new File(["ok"], "cat.png", { type: "image/png" }),
    )
    // The popover hides the rest of the page from the accessibility tree, react-aria calling
    // `ariaHideOutside` on it, so the grid is unreadable until it closes.
    await user.keyboard("{Escape}")

    expect(await screen.findByRole("img", { name: failed.description })).toBeVisible()
    expect(await screen.findByRole("button", { name: "Downloads (0)" })).toBeVisible()
  }, 15_000)
})
