package fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.dtos.input.UserInputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.dtos.output.UserOutputDto
import java.util.UUID.randomUUID

object UserDtoMapper {
    fun UserInputDto.toDomain() =
        User(
            id = randomUUID(),
            name = name,
        )

    fun User.toDto() =
        UserOutputDto(
            id = id,
            name = name,
        )
}
