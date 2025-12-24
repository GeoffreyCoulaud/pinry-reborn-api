package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models

import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID
import java.util.UUID.randomUUID

@Entity
@Table(name = "users")
class SqliteUserModel(
    id: UUID = randomUUID(),
    var name: String = "",
) : SqliteBaseModel(id = id)
