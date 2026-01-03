package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import java.util.UUID

interface PinRepositoryInterface {
    /**
     * Create or update a pin from the given domain data.
     */
    fun savePin(pin: Pin): Pin

    /**
     * Find a pin by its ID
     */
    fun findPinById(id: UUID): Pin?
}
