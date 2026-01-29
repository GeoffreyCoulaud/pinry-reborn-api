package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.CursorDirection.BACKWARD
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.CursorDirection.FORWARD
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.BaseModel
import io.ebean.typequery.QueryBean

class ModelPaginationHelper<M : BaseModel, Q : QueryBean<M, Q>> {
    fun getPage(
        cursor: ModelCursor<M>?,
        pageSize: Int,
        baseQuery: Q,
        sortStrategy: ModelSortStrategy<M, Q>,
    ): ModelPage<M> {
        val direction = cursor?.direction ?: FORWARD

        // +1 to include the cursor (if there is one)
        // +2 to know if there are more than the page in the direction
        val maxRows = pageSize + if (cursor == null) 1 else 2

        var hasMoreInDirection: Boolean = false
        val elements =
            baseQuery
                .let { sortStrategy.filterCursorNeighbors(query = it, cursor = cursor) }
                .let { sortStrategy.sortCursorNeighbors(query = it, cursor = cursor) }
                .setMaxRows(maxRows)
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

        val previousCursor =
            when (direction) {
                FORWARD -> elements.firstOrNull()
                BACKWARD -> elements.lastOrNull()?.takeIf { hasMoreInDirection }
            }?.let { ModelCursor(pivot = it, direction = FORWARD) }

        val nextCursor =
            when (direction) {
                FORWARD -> elements.lastOrNull()?.takeIf { hasMoreInDirection }
                BACKWARD -> elements.firstOrNull()
            }?.let { ModelCursor(pivot = it, direction = FORWARD) }

        return ModelPage(
            items = elements,
            previousCursor = previousCursor,
            nextCursor = nextCursor,
        )
    }
}
