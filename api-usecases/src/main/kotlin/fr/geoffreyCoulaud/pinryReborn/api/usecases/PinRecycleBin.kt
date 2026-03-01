package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinDeletionPermissionError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinDeletionPinAlreadySoftDeletedError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinDeletionPinDoesNotExistError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinDeletionPinNotSoftDeletedError
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PinRecycleBin(
    private val pinRepository: PinRepositoryInterface,
) {
    private fun findPinAndValidateOwnership(pinId: UUID, user: User): Pin {
        val pin = pinRepository.findPinById(id = pinId) ?: throw PinDeletionPinDoesNotExistError()
        if (pin.author != user) throw PinDeletionPermissionError()
        return pin
    }

    fun softDelete(pinId: UUID, user: User): Pin {
        val pin = findPinAndValidateOwnership(pinId, user)
        if (pin.softDeletedAt != null) throw PinDeletionPinAlreadySoftDeletedError()
        return pinRepository.softDeletePin(pin)
    }

    fun restore(pinId: UUID, user: User): Pin {
        val pin = findPinAndValidateOwnership(pinId, user)
        if (pin.softDeletedAt == null) throw PinDeletionPinNotSoftDeletedError()
        return pinRepository.restorePin(pin)
    }

    fun permanentlyDelete(pinId: UUID, user: User) {
        val pin = findPinAndValidateOwnership(pinId, user)
        if (pin.softDeletedAt == null) throw PinDeletionPinNotSoftDeletedError()
        pinRepository.permanentlyDeletePin(pin)
    }

    fun emptyRecycleBin(user: User) {
        pinRepository.permanentlyDeleteAllSoftDeletedPinsForUser(user)
    }
}
