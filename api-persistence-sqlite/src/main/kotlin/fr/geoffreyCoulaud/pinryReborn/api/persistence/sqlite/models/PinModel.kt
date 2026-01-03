package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.AuthoredBaseModel
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "pins")
class PinModel(
    id: UUID,
    author: UserModel,
    var sourceUrl: String,
    var mediaUrl: String,
    var description: String,
) : AuthoredBaseModel(
        id = id,
        author = author,
    )
