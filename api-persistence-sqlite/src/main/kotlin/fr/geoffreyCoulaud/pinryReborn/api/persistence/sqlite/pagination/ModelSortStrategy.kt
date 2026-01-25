package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection.BACKWARD
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection.FORWARD
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.BaseModel
import io.ebean.typequery.QueryBean

abstract class ModelSortStrategy<M : BaseModel, Q : QueryBean<M, Q>> {

    fun filterCursorNeighbors(cursor: M?, query: Q, direction: PaginationDirection): Q {
        if (cursor == null) return query // Neighbors are the first results from the sort.
        return when (direction) {
            FORWARD -> filterCursorForwardNeighbors(cursor, query)
            BACKWARD -> filterCursorBackwardNeighbors(cursor, query)
        }
    }

    protected abstract fun filterCursorForwardNeighbors(cursor: M, query: Q): Q
    protected abstract fun filterCursorBackwardNeighbors(cursor: M, query: Q): Q

    fun sortCursorNeighbors(query: Q, direction: PaginationDirection): Q = when (direction) {
        FORWARD -> sortCursorForwardNeighbors(query)
        BACKWARD -> sortCursorBackwardNeighbors(query)
    }

    protected abstract fun sortCursorForwardNeighbors(query: Q): Q
    protected abstract fun sortCursorBackwardNeighbors(query: Q): Q

}