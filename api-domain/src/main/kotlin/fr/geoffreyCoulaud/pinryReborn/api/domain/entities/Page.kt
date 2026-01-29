package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

data class Page<T>(
    val items: List<T>,
    val previousCursor: Cursor?,
    val nextCursor: Cursor?,
)
