package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PasswordHashAlgorithm
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.BaseModel
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "user_password_hashes")
class UserPasswordHashModel(
    @ManyToOne var user: UserModel,
    var hash: String,
    var algorithm: PasswordHashAlgorithm,
) : BaseModel()
