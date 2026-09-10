import { Navigate } from "@tanstack/react-router"
import { useLayoutEffect, useRef, useState, type RefObject } from "react"
import {
  Button,
  Collection,
  Dialog,
  GridList,
  GridListItem,
  GridListLoadMoreItem,
  Modal,
  ModalOverlay,
  Size,
  Virtualizer,
  WaterfallLayout,
} from "react-aria-components"
import { placeableTiles, renditionForColumn, tileAspectRatio, tileImageSource } from "../lib/tiles"
import { m } from "../paraglide/messages.js"
import { usePins, type Pin } from "../pins"
import { useSession, useSignOut } from "../session"

/**
 * Every bound here is finite, and two of them have to be. `WaterfallLayout` reads the scroll
 * view's width, which react-aria reports as infinite under test, so an unbounded `maxColumns`,
 * `maxItemSize` or `maxHorizontalSpace` lays the grid out at `NaN`. A finite column is also what
 * a tile wants: past the medium rendition the server has no more pixels to give it.
 */
const LAYOUT = {
  minItemSize: new Size(200, 200),
  maxItemSize: new Size(480, 960),
  maxHorizontalSpace: 16,
  maxColumns: 8,
}

/** The column's width, which the layout gives the item and only a browser can measure. */
function useColumnWidth(ref: RefObject<HTMLElement | null>): number {
  const [width, setWidth] = useState(0)
  useLayoutEffect(() => {
    const measured = ref.current?.clientWidth ?? 0
    if (measured !== width) setWidth(measured)
  })
  return width
}

/**
 * The tile carries its ratio so the layout measures it at its true height on the first pass and
 * its column settles once, before a byte of the image arrives (specification 4.7).
 */
function Tile({ pin }: { pin: Pin }) {
  const ref = useRef<HTMLDivElement>(null)
  const columnWidth = useColumnWidth(ref)
  const image = pin.image
  const ratio = { aspectRatio: tileAspectRatio(image?.width, image?.height) }

  return (
    <div ref={ref} className="w-full">
      {image?.url ? (
        <img
          src={tileImageSource(image.url, renditionForColumn(columnWidth, window.devicePixelRatio))}
          alt={pin.description}
          style={ratio}
          className="w-full rounded object-cover"
        />
      ) : (
        // A failed download keeps its tile and says why; a pin with no image at all says what it is.
        <p style={ratio} className="grid place-content-center rounded bg-current/5 p-2 text-center">
          {image?.message ?? pin.description}
        </p>
      )}
    </div>
  )
}

function PinDialog({ pin }: { pin: Pin }) {
  return (
    <div className="flex flex-col gap-3">
      {pin.image?.url && (
        <img
          src={tileImageSource(pin.image.url, "MEDIUM")}
          alt={pin.description}
          className="max-h-[60vh] w-full object-contain"
        />
      )}
      <p>{pin.description}</p>
      <ul className="flex flex-wrap gap-2">
        {pin.tags.map((tag) => (
          <li key={tag.name}>{tag.name}</li>
        ))}
      </ul>
      <ul className="flex flex-wrap gap-2">
        {pin.boards.map((board) => (
          <li key={board.id}>{board.name}</li>
        ))}
      </ul>
      <Button slot="close" className="self-end rounded bg-current/10 px-2 py-1">
        {m.close()}
      </Button>
    </div>
  )
}

function PinGrid() {
  const pins = usePins()
  const [openedId, setOpenedId] = useState<string | null>(null)
  const tiles = placeableTiles(pins.data?.pages.flatMap((page) => page.pins) ?? [])
  const opened = tiles.find((pin) => pin.id === openedId)

  return (
    <>
      <Virtualizer layout={WaterfallLayout} layoutOptions={LAYOUT}>
        <GridList
          aria-label={m.home_heading()}
          layout="grid"
          selectionMode="multiple"
          className="h-full outline-none"
          onAction={(key) => setOpenedId(String(key))}
        >
          <Collection items={tiles}>
            {(pin) => (
              <GridListItem textValue={pin.description}>
                <Tile pin={pin} />
              </GridListItem>
            )}
          </Collection>
          {/*
            The sentinel is re-observed on every collection change, and its own loading flag is
            one such change, so an unguarded `onLoadMore` re-enters until the test times out.
          */}
          <GridListLoadMoreItem
            onLoadMore={() => {
              if (pins.hasNextPage && !pins.isFetchingNextPage) void pins.fetchNextPage()
            }}
            isLoading={pins.isFetchingNextPage}
          />
        </GridList>
      </Virtualizer>
      <ModalOverlay
        isOpen={opened !== undefined}
        onOpenChange={() => setOpenedId(null)}
        isDismissable
        className="fixed inset-0 grid place-items-center bg-black/40 p-4"
      >
        <Modal className="max-h-full w-full max-w-2xl overflow-auto rounded bg-white p-4 dark:bg-neutral-900">
          <Dialog aria-label={opened?.description} className="outline-none">
            {opened && <PinDialog pin={opened} />}
          </Dialog>
        </Modal>
      </ModalOverlay>
    </>
  )
}

export function Home() {
  const session = useSession()
  const signOut = useSignOut()

  if (session.isPending) return null
  if (!session.data) return <Navigate to="/sign-in" />

  return (
    <main className="flex h-screen flex-col gap-4 p-4">
      <header className="flex items-baseline justify-between">
        <h1 className="text-2xl font-semibold">{m.home_heading()}</h1>
        <button type="button" onClick={() => signOut.mutate()}>
          {m.sign_out()}
        </button>
      </header>
      <div className="min-h-0 flex-1">
        <PinGrid />
      </div>
    </main>
  )
}
