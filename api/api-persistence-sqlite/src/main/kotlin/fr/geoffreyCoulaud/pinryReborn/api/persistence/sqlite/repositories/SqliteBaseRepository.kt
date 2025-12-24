package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.SqliteBaseModel
import io.ebean.Database
import jakarta.inject.Inject
import java.util.UUID
import kotlin.reflect.KClass

abstract class SqliteBaseRepository<T : SqliteBaseModel>(
    private val entityClass: KClass<T>,
) {
    @Inject
    protected lateinit var database: Database

    fun findById(id: UUID): T? = database.find(entityClass.java, id)

    fun findAll(): List<T> = database.find(entityClass.java).findList()

    fun save(model: T) = database.save(model)

    fun saveAndReturn(model: T): T = model.also { save(it) }

    fun delete(model: T) = database.delete(model)

    fun deleteById(id: UUID) = database.delete(entityClass.java, id)
}
