import { Navigate } from "@tanstack/react-router"
import { m } from "../paraglide/messages.js"
import { useSession, useSignOut } from "../session"

export function Home() {
  const session = useSession()
  const signOut = useSignOut()

  if (session.isPending) return null
  if (!session.data) return <Navigate to="/sign-in" />

  return (
    <main className="mx-auto max-w-5xl p-8">
      <h1 className="text-2xl font-semibold">{m.home_heading()}</h1>
      <button type="button" onClick={() => signOut.mutate()}>
        {m.sign_out()}
      </button>
    </main>
  )
}
