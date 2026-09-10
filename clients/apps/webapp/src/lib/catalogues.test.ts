import { describe, expect, it } from "vitest"
import en from "../../messages/en.json"
import fr from "../../messages/fr.json"
import { keysMissingFrom } from "./catalogues"

describe("the catalogues", () => {
  it("Given a locale that never translated a message, Then its key is named", () => {
    const incomplete = { greeting: "Bonjour" }
    const complete = { greeting: "Hello", farewell: "Goodbye" }

    expect(keysMissingFrom(incomplete, complete)).toEqual(["farewell"])
  })

  it("Given the two catalogues, Then neither locale is missing a message the other carries", () => {
    expect(keysMissingFrom(fr, en)).toEqual([])
    expect(keysMissingFrom(en, fr)).toEqual([])
  })
})
