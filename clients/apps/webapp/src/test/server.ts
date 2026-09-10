import { setupServer } from "msw/node"

/** No default handler: a journey declares the routes it needs and every other one is an error. */
export const server = setupServer()
