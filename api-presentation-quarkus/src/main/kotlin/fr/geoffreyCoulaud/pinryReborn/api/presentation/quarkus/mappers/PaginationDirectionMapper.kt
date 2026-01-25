package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.PaginationDirectionInputEnum

object PaginationDirectionMapper {
    fun PaginationDirectionInputEnum.toDomain(): PaginationDirection = when (this) {
        PaginationDirectionInputEnum.BACKWARD -> PaginationDirection.BACKWARD
        PaginationDirectionInputEnum.FORWARD -> PaginationDirection.FORWARD
    }
}
