package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.PaginationDirectionInputEnum

object PaginationDirectionMapper {
    fun PaginationDirectionInputEnum?.toDomain(): PaginationDirection = when (this) {
        PaginationDirectionInputEnum.BACKWARD -> PaginationDirection.BACKWARD
        else -> PaginationDirection.FORWARD
    }
}
