package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.AuthoredBaseModel
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "pins")
class PinModel(
    author: UserModel,
    var sourceUrl: String,
    var mediaUrl: String,
    var description: String,
    @ManyToMany(
        cascade = [
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH,
            CascadeType.DETACH
        ]
    )
    var tags: MutableList<TagModel> = mutableListOf(),
) : AuthoredBaseModel(
    author = author,
)
