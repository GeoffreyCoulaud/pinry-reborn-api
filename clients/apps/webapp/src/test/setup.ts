import "@testing-library/jest-dom/vitest"
import { cleanup } from "@testing-library/react"
import { afterAll, afterEach } from "vitest"
import { server } from "./server"

// Interception starts here rather than in `beforeAll`, and the difference is not cosmetic:
// `openapi-fetch` reads `globalThis.fetch` when the client is built, the application builds
// its client while `session.ts` is imported, and a setup file runs before that import while a
// hook runs after it. Started late, every journey reaches the real network and reads whatever
// answers on the test origin. An unhandled request fails the test rather than warning, so a
// route no journey declared cannot pass on a silent network failure.
server.listen({ onUnhandledRequest: "error" })

// Testing Library cleans up by itself only when Vitest exposes its globals, which it does not here.
afterEach(cleanup)
afterEach(() => server.resetHandlers())
afterAll(() => server.close())
