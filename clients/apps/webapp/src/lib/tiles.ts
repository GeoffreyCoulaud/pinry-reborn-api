/** The two renditions a tile ever asks for, of the four the API serves. */
export type Rendition = "SMALL" | "MEDIUM"

/**
 * The widest column, in device pixels, the small rendition covers without stretching. It is
 * `images.renditions.small`'s default, and the handshake of section 4.3 does publish the
 * deployment's own since block 7: nothing reads it here yet, so a deployment that narrows its
 * renditions costs a tile more bytes than it needs rather than a defect.
 */
const SMALL_RENDITION_PX = 240

/**
 * The CSS `aspect-ratio` the tile is placed with. `WaterfallLayout` takes no per-item size and
 * measures the node instead, so a tile that knows its ratio settles into its column at mount,
 * before a byte of the image arrives (specification 4.7, question AA). A tile the API gave no
 * dimensions for is square, which is one measurement like any other.
 */
export function tileAspectRatio(
  width: number | null | undefined,
  height: number | null | undefined,
): string {
  return width != null && height != null ? `${width} / ${height}` : "1 / 1"
}

/** The rendition a column this wide needs, on a display of this pixel ratio. */
export function renditionForColumn(columnWidth: number, pixelRatio: number): Rendition {
  return columnWidth * pixelRatio > SMALL_RENDITION_PX ? "MEDIUM" : "SMALL"
}

/** The bytes an `<img>` fetches: the relative URL the API gave, at one rendition. */
export function tileImageSource(url: string, rendition: Rendition): string {
  return `${url}?size=${rendition}`
}

/**
 * The pins a page places. A download the server is still running has no dimensions, so its tile
 * would reflow the column when it finished: it stays out until the pin carries its image
 * (specification 4.2).
 */
export function placeableTiles<T extends { image?: { status: string } | null }>(
  pins: readonly T[],
): T[] {
  return pins.filter((pin) => pin.image?.status !== "PENDING")
}
