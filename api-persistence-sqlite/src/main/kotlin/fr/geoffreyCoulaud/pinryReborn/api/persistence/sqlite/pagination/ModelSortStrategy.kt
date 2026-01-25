package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.CursorDirection.BACKWARD
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.CursorDirection.FORWARD
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.BaseModel
import io.ebean.typequery.QueryBean

abstract class ModelSortStrategy<M : BaseModel, Q : QueryBean<M, Q>> {
    /**
     * Adapt the query to find the cursor's pivot and neighbors
     */
    fun filterCursorNeighbors(
        query: Q,
        cursor: ModelCursor<M>?,
    ): Q {
        if (cursor == null) return query
        return when (cursor.direction) {
            FORWARD -> filterCursorForwardNeighbors(cursor, query)
            BACKWARD -> filterCursorBackwardNeighbors(cursor, query)
        }
    }

    /**
     * Adapt the query to find the cursor's pivot and its forward neighbors
     */
    protected abstract fun filterCursorForwardNeighbors(
        cursor: ModelCursor<M>,
        query: Q,
    ): Q

    /**
     * Adapt the query to find the cursor's pivot and its backward neighbors
     */
    protected abstract fun filterCursorBackwardNeighbors(
        cursor: ModelCursor<M>,
        query: Q,
    ): Q

    /**
     * Adapt the query to sort the result according to the strategy
     */
    fun sortCursorNeighbors(
        query: Q,
        cursor: ModelCursor<M>?,
    ): Q =
        when (cursor?.direction ?: FORWARD) {
            FORWARD -> sortCursorForwardNeighbors(query)
            BACKWARD -> sortCursorBackwardNeighbors(query)
        }

    /**
     * Adapt the query to sort the result according to the strategy when going forward
     */
    protected abstract fun sortCursorForwardNeighbors(query: Q): Q

    /**
     * Adapt the query to sort the result according to the strategy when going backward
     */
    protected abstract fun sortCursorBackwardNeighbors(query: Q): Q
}
