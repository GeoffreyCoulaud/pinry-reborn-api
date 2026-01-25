package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Page
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PinLister(
    private val pinRepository: PinRepositoryInterface,
) {

    fun listPinsForUserPaginated(
        user: User,
        cursor: UUID?,
        direction: PaginationDirection,
        pageSize: Int,
        sort: PinSortStrategy,
    ): Page<Pin> {
        return pinRepository.findPinsForUserPaginated(
            user = user,
            cursor = cursor,
            direction = direction,
            pageSize = pageSize.coerceIn(1, MAX_PAGE_SIZE),
            sortStrategy = sort,
        )
    }

    companion object {
        const val MAX_PAGE_SIZE = 100
    }
}
