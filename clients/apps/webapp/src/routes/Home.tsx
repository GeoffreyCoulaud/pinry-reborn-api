import { m } from "../paraglide/messages.js"

export function Home() {
  return (
    <main className="mx-auto max-w-5xl p-8">
      <h1 className="text-2xl font-semibold">{m.home_heading()}</h1>
    </main>
  )
}
