package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.UserModel
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class AuthoredBaseModel(
    @ManyToOne var author: UserModel,
) : BaseModel()
