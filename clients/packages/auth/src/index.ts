import { createApiClient, type ApiClient, type components } from "@pinry-reborn/api-client"

/** The vehicle the session token travels in. The web application takes the cookie, the extension the header. */
export type SessionTransport = components["schemas"]["SessionTransport"]

export type Credentials = components["schemas"]["UserInputDto"]

/**
 * What both transports agree on. A bearer answer also carries the token, which never leaves this
 * package: the application above holds no credential whatever the transport
 * (docs/adr/0026-one-session-two-transports.md, decision 1).
 */
export interface Session { expiresAt: string; renewAfter: string }

export interface AuthOptions {
  transport: SessionTransport
  /** The origin the API answers on: its own for a caller that shares it, the deployment's for the extension. */
  baseUrl: string
}

export interface Auth {
  /** Creates the account and opens its first session, so a fresh instance signs the user in once. */
  signUp(credentials: Credentials, rememberMe?: boolean): Promise<Session>
  signIn(credentials: Credentials, rememberMe?: boolean): Promise<Session>
  signOut(): Promise<void>
  /** The session the request already carries, or null when the API no longer honours it. */
  currentSession(): Promise<Session | null>
}

export function createAuth({ transport, baseUrl }: AuthOptions): Auth {
  const client: ApiClient = createApiClient(baseUrl)
  let token: string | undefined
  client.use({
    onRequest({ request }) {
      if (token !== undefined) request.headers.set("Authorization", `Bearer ${token}`)
      return request
    },
  })

  async function openSession(credentials: Credentials, rememberMe: boolean): Promise<Session> {
    const { data, response } = await client.POST("/api/v1/sessions", {
      body: { ...credentials, transport, rememberMe },
    })
    // A cookie answer is 200 and a bearer answer 201, so the token is absent by shape and not by
    // a nullable field (docs/adr/0026-one-session-two-transports.md, decision 4).
    if (data === undefined) throw new Error(`The API refused the session: ${response.status}.`)
    if ("token" in data) token = data.token
    return { expiresAt: data.expiresAt, renewAfter: data.renewAfter }
  }

  return {
    async signUp(credentials, rememberMe = false) {
      const { data, response } = await client.POST("/api/v1/users", { body: credentials })
      if (data === undefined) throw new Error(`The API refused the account: ${response.status}.`)
      return openSession(credentials, rememberMe)
    },
    signIn: (credentials, rememberMe = false) => openSession(credentials, rememberMe),
    async signOut() {
      // The token goes whatever the answer: a revocation the API refuses is still a user who
      // asked to leave, and the cookie is the server's to clear.
      await client.DELETE("/api/v1/sessions/current")
      token = undefined
    },
    async currentSession() {
      const { data } = await client.GET("/api/v1/sessions/current")
      return data === undefined ? null : { expiresAt: data.expiresAt, renewAfter: data.renewAfter }
    },
  }
}
