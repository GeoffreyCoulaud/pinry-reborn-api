package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.SqliteBaseModel
import io.ebean.BeanRepository
import io.ebean.Database
import java.util.UUID
import kotlin.reflect.KClass

internal class GenericRepository<T : SqliteBaseModel>(
    entityClass: KClass<T>,
    database: Database,
) : BeanRepository<UUID, T>(entityClass.java, database) {
    fun saveAndReturn(model: T): T = model.also { save(it) }
}
