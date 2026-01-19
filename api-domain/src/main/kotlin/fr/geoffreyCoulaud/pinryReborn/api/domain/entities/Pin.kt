package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

import java.util.*

data class Pin(
    val id: UUID,
    val author: User,
    val sourceContextUrl: String,
    val sourceMediaUrl: String,
    val description: String,
    val tags: List<Tag>
)