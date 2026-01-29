package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.common.CursorDirectionDto
import java.util.UUID

class CursorOutputDto(
    val pivotId: UUID,
    val direction: CursorDirectionDto,
)
