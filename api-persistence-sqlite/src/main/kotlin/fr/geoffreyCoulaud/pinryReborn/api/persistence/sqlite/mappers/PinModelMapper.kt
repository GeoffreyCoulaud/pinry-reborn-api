package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.TagModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.TagModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel

object PinModelMapper {
    fun Pin.toModel(): PinModel =
        PinModel(
            author = author.toModel(),
            sourceUrl = sourceUrl,
            mediaUrl = mediaUrl,
            description = description,
            tags = tags
                .map { it.toModel() }
                .toMutableList()
        )

    fun PinModel.toDomain(): Pin =
        Pin(
            id = id,
            author = author.toDomain(),
            sourceUrl = sourceUrl,
            mediaUrl = mediaUrl,
            description = description,
            tags = tags.map { it.toDomain() },
        )
}
