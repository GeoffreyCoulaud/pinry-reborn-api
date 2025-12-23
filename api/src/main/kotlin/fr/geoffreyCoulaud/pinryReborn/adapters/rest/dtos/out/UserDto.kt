package fr.geoffreyCoulaud.pinryReborn.adapters.rest.dtos.out

import java.util.UUID

data class UserDto(
    val id: UUID,
    val name: String,
)
