package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Page
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PaginationOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinListOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.BoardMapper.toRefDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.CursorMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.PinImageStateMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.TagMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinImageState
import java.util.UUID

object PinMapper {
    /**
     * [imageStates] is what `ResolvePinImageState.statesFor` returned for the pins being mapped: a
     * parameter rather than a default, so the compiler names every response that forgot to resolve it.
     */
    fun Pin.toDto(imageStates: Map<UUID, PinImageState>) = PinOutputDto(
        id = id,
        authorId = author.id,
        sourceContextUrl = sourceContextUrl,
        sourceMediaUrl = sourceMediaUrl,
        description = description,
        tags = tags.map { it.toDto() },
        boards = boards.map { it.toRefDto() },
        softDeletedAt = softDeletedAt,
        image = imageStates[id]?.toDto(id),
    )

    fun Page<Pin>.toDto(imageStates: Map<UUID, PinImageState>) = PinListOutputDto(
        pins = this.items.map { it.toDto(imageStates) },
        pagination = PaginationOutputDto(
            previousCursor = this.previousCursor?.toDto(),
            nextCursor = this.nextCursor?.toDto(),
        ),
    )
}


