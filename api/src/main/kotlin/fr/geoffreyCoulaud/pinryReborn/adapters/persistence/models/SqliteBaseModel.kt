package fr.geoffreyCoulaud.pinryReborn.adapters.persistence.models

import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID

@MappedSuperclass
class SqliteBaseModel(
    @Id var id: UUID = randomUUID(),
) {
    @WhenCreated
    lateinit var whenCreated: Instant

    @WhenModified
    lateinit var whenModified: Instant
}
