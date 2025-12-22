package fr.geoffreyCoulaud.pinryReborn.api.application.entities

import java.net.URL
import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

data class Pin(
    val id: UUID = randomUUID(),
    val createdAt: Instant = now(),
    val owner: User,
    val sourceURL: URL,
    val board: Board,
    val tags: List<Tag> = emptyList(),
)
