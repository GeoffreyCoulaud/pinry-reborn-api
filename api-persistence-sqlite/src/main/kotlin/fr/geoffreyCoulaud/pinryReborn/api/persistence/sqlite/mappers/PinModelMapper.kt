package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel

object PinModelMapper {
    fun Pin.toModel(): PinModel =
        PinModel(
            id = id,
            author = author.toModel(),
            sourceUrl = sourceContextUrl,
            mediaUrl = sourceMediaUrl,
            description = description,
        )

    fun PinModel.toDomain(tags: List<Tag>): Pin =
        Pin(
            id = id,
            author = author.toDomain(),
            sourceContextUrl = sourceUrl,
            sourceMediaUrl = mediaUrl,
            description = description,
            tags = tags,
        )
}
