import { createAuth, type Credentials, type Session } from "@pinry-reborn/auth"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"

/**
 * The application and the API are one origin behind the proxy, so the application's own is the
 * API's, and the cookie is the transport a browser can send on an `<img>`
 * (docs/adr/0026-one-session-two-transports.md, decisions 3 and 5).
 */
const auth = createAuth({ transport: "COOKIE", baseUrl: window.location.origin })

const SESSION_KEY = ["session"]

export interface OpenSession {
  credentials: Credentials
  rememberMe: boolean
}

/** The session the browser's cookie carries, or null once the API stops honouring it. */
export function useSession() {
  // A refusal is an answer and not a failure to retry: an expired session never becomes valid again.
  return useQuery({ queryKey: SESSION_KEY, queryFn: () => auth.currentSession(), retry: false })
}

function useOpenSession(open: (credentials: Credentials, rememberMe: boolean) => Promise<Session>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ credentials, rememberMe }: OpenSession) => open(credentials, rememberMe),
    onSuccess: (session) => queryClient.setQueryData(SESSION_KEY, session),
  })
}

export type OpenSessionMutation = ReturnType<typeof useOpenSession>

export function useSignIn(): OpenSessionMutation {
  return useOpenSession((credentials, rememberMe) => auth.signIn(credentials, rememberMe))
}

export function useSignUp(): OpenSessionMutation {
  return useOpenSession((credentials, rememberMe) => auth.signUp(credentials, rememberMe))
}

export function useSignOut() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => auth.signOut(),
    onSuccess: () => queryClient.setQueryData(SESSION_KEY, null),
  })
}
