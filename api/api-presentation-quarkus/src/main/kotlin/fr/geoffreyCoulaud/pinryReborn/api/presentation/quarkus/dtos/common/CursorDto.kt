package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.common

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.UUID

/**
 * The decoded form of a cursor. On the wire it is the Base64 string `Base64Json` builds, and the
 * schema says so: SmallRye knows neither that annotation nor `Base64JsonSerializer`, so left to
 * derive the schema from this type it would publish the object clients must never read.
 */
@Schema(
    type = SchemaType.STRING,
    description = "Opaque pagination cursor. Read it from a response and send it back unchanged.",
)
class CursorDto(
    val pivotId: UUID,
    val direction: CursorDirectionDto,
)
