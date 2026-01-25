package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Cursor
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.CursorDirection
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.common.CursorDirectionDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.common.CursorDto

object CursorMapper {
    fun CursorDto.toDomain(): Cursor =
        Cursor(
            pivotId = pivotId,
            direction = direction.toDomain(),
        )

    fun CursorDirectionDto.toDomain(): CursorDirection =
        when (this) {
            CursorDirectionDto.BACKWARD -> CursorDirection.BACKWARD
            CursorDirectionDto.FORWARD -> CursorDirection.FORWARD
        }

    fun CursorDirection.toDto(): CursorDirectionDto =
        when (this) {
            CursorDirection.FORWARD -> CursorDirectionDto.FORWARD
            CursorDirection.BACKWARD -> CursorDirectionDto.BACKWARD
        }

    fun Cursor.toDto(): CursorDto =
        CursorDto(
            pivotId = pivotId,
            direction = direction.toDto(),
        )
}
