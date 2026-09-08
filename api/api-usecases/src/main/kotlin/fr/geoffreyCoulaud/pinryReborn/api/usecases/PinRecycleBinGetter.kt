package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Cursor
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Page
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PinRecycleBinGetter(
    private val pinRepository: PinRepositoryInterface,
    private val pinGetter: PinGetter,
) {
    fun listSoftDeletedPinsPaginatedForUser(
        reader: User,
        cursor: Cursor?,
        pageSize: Int,
        sort: PinSortStrategy,
    ): Page<Pin> {
        if (cursor != null) {
            pinGetter.getPinForUser(pinId = cursor.pivotId, reader = reader)
        }
        return pinRepository.findSoftDeletedPinsForUser(
            reader = reader,
            cursor = cursor,
            pageSize = pageSize.coerceIn(1, PinGetter.MAX_PAGE_SIZE),
            sortStrategy = sort,
        )
    }
}
