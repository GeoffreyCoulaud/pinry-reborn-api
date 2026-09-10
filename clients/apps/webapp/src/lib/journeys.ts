/**
 * The user journeys the gate requires a test for. A block adds its own and none is ever
 * removed (docs/specs/2026-09-10-web-application.md, section 4.6).
 */
export const REQUIRED_JOURNEYS = [
  "open the application",
  "sign up",
  "sign in",
  "sign out",
  "session expiry",
]

/** The file under `src/journeys/` that holds a journey's test. */
export function journeyTestFile(journey: string): string {
  return `${journey.replaceAll(" ", "-")}.journey.test.tsx`
}
