package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinRetrievalPermissionError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinRetrievalPinDoesNotExistError
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PinGetter(
    private val pinRepository: PinRepositoryInterface,
) {
    fun getPinForUser(
        pinId: UUID,
        reader: User,
    ): Pin {
        val pin = pinRepository.findPinById(id = pinId) ?: throw PinRetrievalPinDoesNotExistError()
        if (pin.author != reader) throw PinRetrievalPermissionError()
        return pin
    }
}
