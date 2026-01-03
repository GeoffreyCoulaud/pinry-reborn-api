package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.AuthoredBaseModel
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "tags")
class TagModel(
    author: UserModel,
    val name: String,
    @ManyToMany(mappedBy = "tags")
    val pins: MutableList<PinModel> = mutableListOf(),
) : AuthoredBaseModel(author = author)
