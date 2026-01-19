package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.TagMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.UserDtoMapper.toDto

object PinMapper {
    fun Pin.toDto() = PinOutputDto(
        id = id,
        author = author.toDto(),
        sourceContextUrl = sourceContextUrl,
        sourceMediaUrl = sourceMediaUrl,
        description = description,
        tags = tags.map { it.toDto() }
    )
}


