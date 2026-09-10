import js from "@eslint/js"
import globals from "globals"
import tseslint from "typescript-eslint"

export default tseslint.config(
  // Generated output and build products: nobody rereads them, and Paraglide's carries its
  // own eslint-disable banner precisely because it is not written by hand.
  { ignores: ["**/dist/", "**/coverage/", "**/src/paraglide/"] },
  js.configs.recommended,
  tseslint.configs.recommended,
  { languageOptions: { globals: globals.browser } },
)
