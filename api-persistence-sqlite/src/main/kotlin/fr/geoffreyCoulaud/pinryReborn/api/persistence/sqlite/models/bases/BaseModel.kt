package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases

import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID

@MappedSuperclass
class BaseModel(
    @Id var id: UUID = randomUUID(),
) {
    @WhenCreated
    lateinit var whenCreated: Instant

    @WhenModified
    lateinit var whenModified: Instant
}
