package fr.geoffreyCoulaud.pinryReborn.api.application.entities

import java.util.UUID

data class Board(
    val id: UUID,
    val name: String,
    val author: User,
)
