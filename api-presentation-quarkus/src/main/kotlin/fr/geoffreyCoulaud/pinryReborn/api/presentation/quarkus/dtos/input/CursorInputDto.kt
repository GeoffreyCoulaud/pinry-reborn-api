package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input

import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.common.CursorDirectionDto
import java.util.UUID

data class CursorInputDto(
    val pivotId: UUID,
    val direction: CursorDirectionDto,
)
