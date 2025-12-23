package fr.geoffreyCoulaud.pinryReborn.adapters.rest.mappers

import fr.geoffreyCoulaud.pinryReborn.adapters.rest.dtos.input.UserInputDto
import fr.geoffreyCoulaud.pinryReborn.adapters.rest.dtos.output.UserOutputDto
import fr.geoffreyCoulaud.pinryReborn.domain.entities.User
import java.util.UUID.randomUUID

object UserDtoMapper {
    fun UserInputDto.toDomain() =
        User(
            id = randomUUID(), // When creating a user, a random UUID is chosen for it
            name = name,
        )

    fun User.toDto() =
        UserOutputDto(
            id = id,
            name = name,
        )
}
