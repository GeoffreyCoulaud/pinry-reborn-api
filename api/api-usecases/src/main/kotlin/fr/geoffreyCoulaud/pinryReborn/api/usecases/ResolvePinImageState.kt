package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.ImageDownloadRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.ImageRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TransactionRunner
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.ImagePermissionError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.ImagePinDoesNotExistError
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class ResolvePinImageState(
    private val pinRepository: PinRepositoryInterface,
    private val imageRepository: ImageRepositoryInterface,
    private val imageDownloadRepository: ImageDownloadRepositoryInterface,
    private val transactionRunner: TransactionRunner,
) {
    fun resolve(pinId: UUID, requester: User): PinImageState {
        val pin = pinRepository.findPinById(pinId) ?: throw ImagePinDoesNotExistError()
        if (pin.author.id != requester.id) throw ImagePermissionError()
        // One snapshot for both reads: the swap commits the new image and the replacement's removal together.
        return transactionRunner.inTransaction {
            PinImageState.derive(imageRepository.findByPinId(pinId), imageDownloadRepository.findByPinId(pinId))
        }
    }

    /**
     * The image state of [pins], keyed by pin id, in two reads whatever the page holds. A pin with
     * neither an image nor a download is absent, so a caller renders it as "no image".
     *
     * No permission check: the caller passes pins a reader-scoped query already returned, unlike
     * [resolve], which is reached by pin id alone.
     */
    fun statesFor(pins: Collection<Pin>): Map<UUID, PinImageState> {
        if (pins.isEmpty()) return emptyMap()
        val pinIds = pins.map { it.id }
        // Same snapshot as above, for the same reason: a swap must not be read half done.
        return transactionRunner.inTransaction {
            val images = imageRepository.findByPinIds(pinIds)
            val downloads = imageDownloadRepository.findByPinIds(pinIds)
            pinIds
                .associateWith { PinImageState.derive(images[it], downloads[it]) }
                .filterValues { it.status != PinImageStatus.NONE }
        }
    }
}
