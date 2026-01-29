package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.CursorDirection
import java.util.UUID

data class Cursor(
    val pivotId: UUID,
    val direction: CursorDirection,
)
