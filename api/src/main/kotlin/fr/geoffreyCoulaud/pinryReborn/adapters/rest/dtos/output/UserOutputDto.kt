package fr.geoffreyCoulaud.pinryReborn.adapters.rest.dtos.output

import java.util.UUID

data class UserOutputDto(
    val id: UUID,
    val name: String,
)
