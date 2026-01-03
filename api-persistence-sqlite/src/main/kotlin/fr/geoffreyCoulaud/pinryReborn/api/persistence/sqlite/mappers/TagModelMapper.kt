package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.TagModel

object TagModelMapper {
    fun Tag.toModel() =
        TagModel(
            author = author.toModel(),
            name = name,
            pins = mutableListOf(),
        )

    fun TagModel.toDomain() =
        Tag(
            id = id,
            author = author.toDomain(),
            name = name,
        )
}
