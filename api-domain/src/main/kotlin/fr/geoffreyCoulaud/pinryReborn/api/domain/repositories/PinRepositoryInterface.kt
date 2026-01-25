package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Page
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
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

    /**
     * Find a pin by its ID, for a given user
     * Read-permissions must be given to the user for them to find the pin, if it exists.
     */
    fun findPinByIdForUser(id: UUID, reader: User): Pin?

    /**
     * Find pins for a given user with pagination support
     * @param user The user whose pins to find
     * @param cursor The pin ID to start from (null for first page)
     * @param direction The pagination direction (forward or backward)
     * @param pageSize Number of pins to return (will be capped at server max)
     * @param sortStrategy The sort strategy
     * @return A page of pins with pagination information
     */
    fun findPinsForUserPaginated(
        user: User,
        cursor: UUID?,
        direction: PaginationDirection,
        pageSize: Int,
        sortStrategy: PinSortStrategy,
    ): Page<Pin>
}
