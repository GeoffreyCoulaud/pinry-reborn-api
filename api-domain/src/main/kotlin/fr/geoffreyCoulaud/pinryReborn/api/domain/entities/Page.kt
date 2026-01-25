package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

import java.util.UUID

data class Page<T>(
    val items: List<T>,
    val previousCursor: UUID?,
    val nextCursor: UUID?,
)
