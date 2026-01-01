package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

import java.util.UUID

data class UserOutputDto(
    val id: UUID,
    val name: String,
)
