package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinTaggingPermissionError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinTaggingPinDoesNotExistError
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PinTagger(
    private val tagCreator: TagCreator,
    private val pinRepository: PinRepositoryInterface,
) {
    fun setTags(
        pinId: UUID,
        tagNames: List<String>,
        user: User,
    ): Pin {
        val pin = pinRepository.findPinById(id = pinId) ?: throw PinTaggingPinDoesNotExistError()
        if (pin.author != user) throw PinTaggingPermissionError()

        val tags = tagNames.map { tagCreator.findOrCreate(name = it, user = user) }
        val updatedPin = pin.copy(tags = tags)
        return pinRepository.savePin(updatedPin)
    }
}
