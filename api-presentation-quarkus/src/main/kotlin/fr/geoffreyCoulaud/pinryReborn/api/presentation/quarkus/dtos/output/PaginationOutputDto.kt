package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.serialization.Base64Json

data class PaginationOutputDto(
    @Base64Json val previousPageCursor: CursorOutputDto?,
    @Base64Json val nextPageCursor: CursorOutputDto?,
)
