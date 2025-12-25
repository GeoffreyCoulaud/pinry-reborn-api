package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models

import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class SqliteUserModel(
    id: UUID,
    var name: String,
) : SqliteBaseModel(id = id)
