package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection.BACKWARD
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection.FORWARD
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.BaseModel
import io.ebean.typequery.QueryBean

class ModelPaginationHelper<M : BaseModel, Q : QueryBean<M, Q>> {

    fun getPage(
        cursor: Cursor<M>?,
        pageSize: Int,
        baseQuery: Q,
        sortStrategy: ModelSortStrategy<M, Q>
    ): ModelPage<M> {
        val direction = cursor?.direction ?: FORWARD
        val hasMoreInDirection: Boolean
        val elements = baseQuery
            .let { sortStrategy.filterCursorNeighbors(query = it, cursor = cursor) }
            .let { sortStrategy.sortCursorNeighbors(query = it, cursor = cursor) }
            .setMaxRows(pageSize + 2) // +2 to know if there are more than the page and include cursor
            .findList()
            .let {
                // Determine if there are more pages
                hasMoreInDirection = it.size > pageSize
                if (hasMoreInDirection) it.dropLast(1) else it
            }
            // Reorder the elements if backward to preserve order
            .let { if (direction == BACKWARD) it.reversed() else it }
            // Remove the cursor
            .let { it.filterNot { el -> el.id == cursor?.pivot?.id } }

        return ModelPage(
            items = elements,
            previousCursor = when (direction) {
                FORWARD -> elements.firstOrNull()?.id
                BACKWARD -> elements.lastOrNull()?.id?.takeIf { hasMoreInDirection }
            },
            nextCursor = when (direction) {
                FORWARD -> elements.lastOrNull()?.id?.takeIf { hasMoreInDirection }
                BACKWARD -> elements.firstOrNull()?.id
            },
        )
    }
}

