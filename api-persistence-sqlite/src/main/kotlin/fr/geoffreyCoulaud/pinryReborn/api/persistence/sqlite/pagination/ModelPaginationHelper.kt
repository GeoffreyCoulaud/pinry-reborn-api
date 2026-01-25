package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection.BACKWARD
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection.FORWARD
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.BaseModel
import io.ebean.typequery.QueryBean
import java.util.UUID

class ModelPaginationHelper<M : BaseModel, Q : QueryBean<M, Q>> {

    fun getPage(
        cursor: M?,
        pageSize: Int,
        direction: PaginationDirection,
        baseQuery: Q,
        sortStrategy: ModelSortStrategy<M, Q>
    ): ModelPage<M> {
        val query = baseQuery
            .let { sortStrategy.filterCursorNeighbors(cursor = cursor, query = it, direction = direction) }
            .let { sortStrategy.sortCursorNeighbors(query = it, direction = direction) }
            .setMaxRows(pageSize + 1) // +1 to know if there are more

        val hasMoreInDirection: Boolean
        val elements = query.setMaxRows(pageSize + 1)
            .findList()
            .apply {
                // Determine if there are more pages
                hasMoreInDirection = size > pageSize
                if (hasMoreInDirection) dropLast(1)
            }
            .apply {
                // For backward direction, reverse the results to maintain consistent order
                if (direction == BACKWARD) reverse()
            }

        // Determine cursors for next/previous pages
        val nextCursor: UUID? = when (direction) {
            FORWARD -> if (hasMoreInDirection) elements.lastOrNull()?.id else null
            BACKWARD -> elements.lastOrNull()?.id
        }
        val previousCursor: UUID? = when (direction) {
            FORWARD -> elements.firstOrNull()?.id
            BACKWARD -> if (hasMoreInDirection) elements.firstOrNull()?.id else null

        }

        return ModelPage(
            items = elements,
            previousCursor = previousCursor,
            nextCursor = nextCursor,
        )
    }
}

