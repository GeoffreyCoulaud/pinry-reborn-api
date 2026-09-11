package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.ImageDownload
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadStatus
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.ImageDownloadRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TransactionRunner
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.ImageDownloadDoesNotExistError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.ImageDownloadInProgressError
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * What a requester still has to watch or to clear: a download row lives while the fetch runs and
 * after it failed, and success deletes it, so the list is exactly the work whose result is not a
 * pin yet (spec `docs/specs/2026-09-10-web-application.md`, section 4.4).
 */
@ApplicationScoped
class ImageDownloads(
    private val imageDownloadRepository: ImageDownloadRepositoryInterface,
    private val transactionRunner: TransactionRunner,
) {
    fun list(requester: User): List<ImageDownload> = imageDownloadRepository.findByAuthor(requester.id)

    /**
     * Drops one settled row: what the requester cannot see is absent to them, and a running row
     * belongs to the worker. Read and delete share a transaction, the requester being free to
     * request the same download again while this one runs.
     */
    fun delete(requester: User, pinId: UUID): Unit = transactionRunner.inTransaction {
        val download = list(requester).firstOrNull { it.pinId == pinId } ?: throw ImageDownloadDoesNotExistError()
        if (download.status == DownloadStatus.PENDING) throw ImageDownloadInProgressError()
        imageDownloadRepository.deleteByPinId(pinId)
    }
}
