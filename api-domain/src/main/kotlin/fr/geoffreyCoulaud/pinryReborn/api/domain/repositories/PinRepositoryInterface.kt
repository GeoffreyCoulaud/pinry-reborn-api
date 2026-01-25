package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Page
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import java.util.*

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
     * Find all pins for a given user
     */
    fun findAllPinsForUser(user: User): List<Pin>

    /**
     * Find pins for a given user with pagination support
     * @param user The user whose pins to find
     * @param cursor The pin ID to start from (null for first page)
     * @param direction The pagination direction (forward or backward)
     * @param pageSize Number of pins to return (will be capped at server max)
     * @param sort The sort strategy
     * @return A page of pins with pagination information
     */
    fun findPinsForUserPaginated(
        user: User,
        cursor: UUID?,
        direction: PaginationDirection,
        pageSize: Int,
        sort: PinSortStrategy,
    ): Page<Pin>
}
