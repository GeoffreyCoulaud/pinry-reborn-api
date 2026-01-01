package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import java.util.UUID

interface PinRepositoryInterface {
    fun savePin(pin: Pin): Pin

    fun findPinById(id: UUID): Pin?

    fun findAllUserPins(user: User): List<Pin>
}
