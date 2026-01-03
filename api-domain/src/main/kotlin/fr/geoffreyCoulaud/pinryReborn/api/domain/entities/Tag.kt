package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

import java.util.UUID

data class Tag(
    val id: UUID,
    val author: User,
    val name: String,
)
