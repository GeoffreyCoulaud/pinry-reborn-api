package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.SqliteBaseModel
import io.ebean.DB.save
import io.ebean.Database
import kotlin.reflect.KClass

class SqlRepository<T : SqliteBaseModel>(
    private val entityClass: KClass<T>,
    private val database: Database,
) {
    fun saveAndReturn(model: T): T = model.also { save(it) }
}
