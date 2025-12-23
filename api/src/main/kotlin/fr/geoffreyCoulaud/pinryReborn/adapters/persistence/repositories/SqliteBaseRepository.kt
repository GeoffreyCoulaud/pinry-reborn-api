package fr.geoffreyCoulaud.pinryReborn.adapters.persistence.repositories

import fr.geoffreyCoulaud.pinryReborn.adapters.persistence.models.SqliteBaseModel
import io.ebean.BeanRepository
import io.ebean.Database
import java.util.UUID
import kotlin.reflect.KClass

abstract class SqliteBaseRepository<T : SqliteBaseModel>(
    klass: KClass<T>,
    database: Database,
) : BeanRepository<UUID, T>(klass.java, database) {
    fun saveAndReturn(model: T): T = model.also { save(it) }
}
