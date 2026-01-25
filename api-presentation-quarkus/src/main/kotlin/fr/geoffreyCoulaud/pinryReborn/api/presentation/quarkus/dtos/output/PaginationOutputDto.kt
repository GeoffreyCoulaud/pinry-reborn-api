package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.common.CursorDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.serialization.Base64Json

data class PaginationOutputDto(
    @Base64Json val previousCursor: CursorDto?,
    @Base64Json val nextCursor: CursorDto?,
)
