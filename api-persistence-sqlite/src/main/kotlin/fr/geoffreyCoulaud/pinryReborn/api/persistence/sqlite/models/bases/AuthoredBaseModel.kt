package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.UserModel
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import java.util.UUID

@MappedSuperclass
abstract class AuthoredBaseModel(
    id: UUID,
    @ManyToOne var author: UserModel,
) : BaseModel(id = id)
