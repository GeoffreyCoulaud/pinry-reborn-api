import { createAuth } from "@pinry-reborn/auth"

/**
 * The application and the API are one origin behind the proxy, so the application's own is the
 * API's, and the cookie is the transport a browser can send on an `<img>`
 * (docs/adr/0026-one-session-two-transports.md, decisions 3 and 5).
 */
export const auth = createAuth({ transport: "COOKIE", baseUrl: window.location.origin })
