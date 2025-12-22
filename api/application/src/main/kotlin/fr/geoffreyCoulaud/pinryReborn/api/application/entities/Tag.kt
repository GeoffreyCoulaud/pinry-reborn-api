package fr.geoffreyCoulaud.pinryReborn.api.application.entities

import java.util.UUID
import java.util.UUID.randomUUID

data class Tag(
    val id: UUID = randomUUID(),
    val name: String,
)
