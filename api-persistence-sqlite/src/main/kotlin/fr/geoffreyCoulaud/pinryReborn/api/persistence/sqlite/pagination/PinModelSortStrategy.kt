package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QPinModel

sealed class PinModelSortStrategy : ModelSortStrategy<PinModel, QPinModel>() {

    /**
     * Sort strategy used to get pages of `PinModel`s in ascending creation date.
     */
    class CreatedAtAsc : PinModelSortStrategy() {
        override fun filterCursorForwardNeighbors(cursor: Cursor<PinModel>, query: QPinModel): QPinModel =
            query.whenCreated.greaterOrEqualTo(cursor.pivot.whenCreated)


        override fun filterCursorBackwardNeighbors(cursor: Cursor<PinModel>, query: QPinModel): QPinModel =
            query.whenCreated.lessOrEqualTo(cursor.pivot.whenCreated)

        override fun sortCursorForwardNeighbors(query: QPinModel): QPinModel {
            return query.orderBy().whenCreated.asc().id.asc()
        }

        override fun sortCursorBackwardNeighbors(query: QPinModel): QPinModel =
            query.orderBy().whenCreated.desc().id.desc()

    }

    /**
     * Sort strategy used to get pages of `PinModel`s in descending creation date.
     */
    class CreatedAtDesc : PinModelSortStrategy() {
        override fun filterCursorForwardNeighbors(cursor: Cursor<PinModel>, query: QPinModel): QPinModel =
            query.whenCreated.lessOrEqualTo(cursor.pivot.whenCreated)


        override fun filterCursorBackwardNeighbors(cursor: Cursor<PinModel>, query: QPinModel): QPinModel =
            query.whenCreated.greaterOrEqualTo(cursor.pivot.whenCreated)


        override fun sortCursorForwardNeighbors(query: QPinModel): QPinModel =
            query.orderBy().whenCreated.desc().id.desc()

        override fun sortCursorBackwardNeighbors(query: QPinModel): QPinModel =
            query.orderBy().whenCreated.asc().id.asc()

    }

    companion object {
        fun fromDomain(strategy: PinSortStrategy) = when (strategy) {
            PinSortStrategy.CREATED_AT_ASC -> CreatedAtAsc()
            PinSortStrategy.CREATED_AT_DESC -> CreatedAtDesc()
        }
    }
}