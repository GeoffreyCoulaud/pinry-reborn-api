package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models

import io.ebean.annotation.DbForeignKey
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "image_download")
@Suppress("LongParameterList") // Ebean entity: every parameter is a persisted column.
class ImageDownloadModel(
    @Id var pinId: UUID,
    var sourceUrl: String,
    var status: String,
    var reasonCode: String?,
    var lastError: String?,
    var taskId: UUID,
    var requestedAt: Instant,
    var updatedAt: Instant,
) {
    /**
     * The pin [pinId] names, so a query about it is a join rather than raw SQL. Read-only on the
     * column [pinId] writes; `docs/specs/2026-09-10-web-application.md` section 4.11 says why.
     */
    @ManyToOne
    @DbForeignKey(noIndex = true)
    @JoinColumn(name = "pin_id", insertable = false, updatable = false)
    lateinit var pin: PinModel
}
