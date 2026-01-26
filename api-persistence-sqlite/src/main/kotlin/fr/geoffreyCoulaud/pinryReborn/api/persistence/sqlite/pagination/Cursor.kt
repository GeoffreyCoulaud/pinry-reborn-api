package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.BaseModel

data class Cursor<M : BaseModel>(
    val pivot: M,
    val direction: PaginationDirection
)
