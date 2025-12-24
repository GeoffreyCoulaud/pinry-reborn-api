package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

import java.util.UUID
import java.util.UUID.randomUUID

data class User(
    val id: UUID = randomUUID(),
    val name: String,
)
