package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

data class SearchResult<T>(
    val item: T,
    val score: Double,
)
