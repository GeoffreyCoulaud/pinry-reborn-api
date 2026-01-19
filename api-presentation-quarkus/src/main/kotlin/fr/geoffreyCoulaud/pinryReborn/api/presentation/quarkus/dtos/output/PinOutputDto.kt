package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

import java.util.*

data class PinOutputDto(
    val id: UUID,
    val author: UserOutputDto,
    val sourceContextUrl: String,
    val sourceMediaUrl: String,
    val description: String,
    val tags: List<TagOutputDto>,
)
