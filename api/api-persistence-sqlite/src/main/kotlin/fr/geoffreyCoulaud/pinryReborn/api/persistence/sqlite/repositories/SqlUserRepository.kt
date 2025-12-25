package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepository
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.SqliteUserModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.query.QSqliteUserModel
import io.ebean.Database
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class SqlUserRepository(
    database: Database,
) : UserRepository {
    /**
     * When possible, avoid using the SQL repository directly.
     *
     * Favor usage of ebean's Query Beans.
     * https://ebean.io/docs/query/query-beans
     */
    private val sqlRepository = SqlRepository(entityClass = SqliteUserModel::class, database = database)

    override fun findUser(id: UUID): User? =
        QSqliteUserModel()
            .id
            .equalTo(id)
            .findOne()
            ?.toDomain()

    override fun saveUser(user: User): User {
        val existing = QSqliteUserModel().id.equalTo(user.id).findOne()
        val model = existing?.updateWith(user) ?: user.toModel()
        return sqlRepository.saveAndReturn(model).toDomain()
    }

    private fun SqliteUserModel.updateWith(user: User) =
        apply {
            name = user.name
        }

    override fun deleteUser(user: User) {
        QSqliteUserModel()
            .id
            .equalTo(user.id)
            .delete()
    }
}
