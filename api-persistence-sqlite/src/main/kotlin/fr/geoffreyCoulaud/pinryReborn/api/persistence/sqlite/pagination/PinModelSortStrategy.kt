package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QPinModel

sealed class PinModelSortStrategy : ModelSortStrategy<PinModel, QPinModel>() {
    /**
     * Sort strategy used to get pages of `PinModel`s in ascending creation date.
     */
    class CreatedAtAsc : PinModelSortStrategy() {
        override fun filterCursorAndForwardNeighbors(
            cursor: ModelCursor<PinModel>,
            query: QPinModel,
        ): QPinModel = query.whenCreated.greaterOrEqualTo(cursor.pivot.whenCreated)

        override fun filterCursorAndBackwardNeighbors(
            cursor: ModelCursor<PinModel>,
            query: QPinModel,
        ): QPinModel = query.whenCreated.lessOrEqualTo(cursor.pivot.whenCreated)

        override fun sortCursorAndForwardNeighbors(query: QPinModel): QPinModel =
            query
                .orderBy()
                .whenCreated
                .asc()

        override fun sortCursorAndBackwardNeighbors(query: QPinModel): QPinModel =
            query
                .orderBy()
                .whenCreated
                .desc()
    }

    /**
     * Sort strategy used to get pages of `PinModel`s in descending creation date.
     */
    class CreatedAtDesc : PinModelSortStrategy() {
        override fun filterCursorAndForwardNeighbors(
            cursor: ModelCursor<PinModel>,
            query: QPinModel,
        ): QPinModel = query.whenCreated.lessOrEqualTo(cursor.pivot.whenCreated)

        override fun filterCursorAndBackwardNeighbors(
            cursor: ModelCursor<PinModel>,
            query: QPinModel,
        ): QPinModel = query.whenCreated.greaterOrEqualTo(cursor.pivot.whenCreated)

        override fun sortCursorAndForwardNeighbors(query: QPinModel): QPinModel =
            query
                .orderBy()
                .whenCreated
                .desc()

        override fun sortCursorAndBackwardNeighbors(query: QPinModel): QPinModel =
            query
                .orderBy()
                .whenCreated
                .asc()
    }

    /**
     * Sort strategy used to get pages of `PinModel`s in descending soft-deletion date.
     */
    class DeletedAtDesc : PinModelSortStrategy() {
        override fun filterCursorAndForwardNeighbors(
            cursor: ModelCursor<PinModel>,
            query: QPinModel,
        ): QPinModel = query.softDeletedAt.lessOrEqualTo(cursor.pivot.softDeletedAt)

        override fun filterCursorAndBackwardNeighbors(
            cursor: ModelCursor<PinModel>,
            query: QPinModel,
        ): QPinModel = query.softDeletedAt.greaterOrEqualTo(cursor.pivot.softDeletedAt)

        override fun sortCursorAndForwardNeighbors(query: QPinModel): QPinModel =
            query
                .orderBy()
                .softDeletedAt
                .desc()

        override fun sortCursorAndBackwardNeighbors(query: QPinModel): QPinModel =
            query
                .orderBy()
                .softDeletedAt
                .asc()
    }

    companion object {
        fun fromDomain(strategy: PinSortStrategy) =
            when (strategy) {
                PinSortStrategy.CREATED_AT_ASC -> CreatedAtAsc()
                PinSortStrategy.CREATED_AT_DESC -> CreatedAtDesc()
                PinSortStrategy.DELETED_AT_DESC -> DeletedAtDesc()
            }
    }
}
