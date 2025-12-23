package fr.geoffreyCoulaud.pinryReborn.adapters.persistence.mappers

import fr.geoffreyCoulaud.pinryReborn.adapters.persistence.models.SqliteUserModel
import fr.geoffreyCoulaud.pinryReborn.domain.entities.User

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
