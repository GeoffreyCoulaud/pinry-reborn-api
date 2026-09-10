import { Link, useNavigate } from "@tanstack/react-router"
import type { ReactNode } from "react"
import { m } from "../paraglide/messages.js"
import { useSignIn, useSignUp, type OpenSessionMutation } from "../session"

interface CredentialsFormProps {
  /** Names the screen and its button, which ask for the same thing. */
  title: string
  refusal: string
  newPassword: boolean
  session: OpenSessionMutation
  footer: ReactNode
}

const FIELD = "rounded border border-current/30 px-2 py-1"

function CredentialsForm({ title, refusal, newPassword, session, footer }: CredentialsFormProps) {
  const navigate = useNavigate()
  return (
    <main className="mx-auto flex max-w-sm flex-col gap-4 p-8">
      <h1 className="text-2xl font-semibold">{title}</h1>
      <form
        className="flex flex-col gap-3"
        onSubmit={(event) => {
          event.preventDefault()
          const fields = new FormData(event.currentTarget)
          const name = String(fields.get("name"))
          const password = String(fields.get("password"))
          session.mutate(
            { credentials: { name, password }, rememberMe: fields.get("rememberMe") !== null },
            { onSuccess: () => void navigate({ to: "/" }) },
          )
        }}
      >
        <label className="flex flex-col gap-1">
          {m.username()}
          <input name="name" required autoComplete="username" className={FIELD} />
        </label>
        <label className="flex flex-col gap-1">
          {m.password()}
          <input
            name="password"
            type="password"
            required
            autoComplete={newPassword ? "new-password" : "current-password"}
            className={FIELD}
          />
        </label>
        <label className="flex items-center gap-2">
          <input name="rememberMe" type="checkbox" />
          {m.remember_me()}
        </label>
        {session.isError && <p role="alert">{refusal}</p>}
        <button type="submit" disabled={session.isPending} className="rounded bg-current/10 py-1">
          {title}
        </button>
      </form>
      {footer}
    </main>
  )
}

export function SignIn() {
  const signIn = useSignIn()
  return (
    <CredentialsForm
      title={m.sign_in()}
      refusal={m.sign_in_refused()}
      newPassword={false}
      session={signIn}
      footer={<Link to="/sign-up">{m.sign_up()}</Link>}
    />
  )
}

export function SignUp() {
  const signUp = useSignUp()
  return (
    <CredentialsForm
      title={m.sign_up()}
      refusal={m.sign_up_refused()}
      newPassword={true}
      session={signUp}
      footer={<Link to="/sign-in">{m.sign_in()}</Link>}
    />
  )
}
