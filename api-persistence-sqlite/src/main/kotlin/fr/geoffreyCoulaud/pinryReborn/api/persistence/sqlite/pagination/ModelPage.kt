package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.pagination

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.bases.BaseModel
import java.util.UUID

data class ModelPage<M : BaseModel>(
    val items: List<M>,
    val previousCursor: UUID?,
    val nextCursor: UUID?,
)