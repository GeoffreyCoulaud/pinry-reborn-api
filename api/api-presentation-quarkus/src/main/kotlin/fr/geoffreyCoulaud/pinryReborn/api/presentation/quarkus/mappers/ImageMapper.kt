package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Image
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.ImageOutputDto

object ImageMapper {
    fun Image.toDto() = ImageOutputDto(
        id = id,
        pinId = pinId,
        mimeType = mimeType,
        width = width,
        height = height,
        byteSize = byteSize,
        url = "/api/v1/pins/$pinId/image",
    )
}
