package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.SqliteUserModel

object UserModelMapper {
    fun User.toModel() =
        SqliteUserModel(
            id = id,
            name = name,
        )

    fun SqliteUserModel.toDomain() =
        User(
            id = id,
            name = name,
        )
}
