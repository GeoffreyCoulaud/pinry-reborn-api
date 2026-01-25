package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Page
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PinLister(
    private val pinRepository: PinRepositoryInterface,
) {
    companion object {
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE_SIZE = 20
    }

    fun listPinsForUser(user: User): List<Pin> = pinRepository.findAllPinsForUser(user)

    fun listPinsForUserPaginated(
        user: User,
        cursor: UUID?,
        direction: PaginationDirection,
        pageSize: Int,
        sort: PinSortStrategy,
    ): Page<Pin> {
        val effectivePageSize = pageSize.coerceIn(1, MAX_PAGE_SIZE)
        return pinRepository.findPinsForUserPaginated(
            user = user,
            cursor = cursor,
            direction = direction,
            pageSize = effectivePageSize,
            sort = sort,
        )
    }
}
