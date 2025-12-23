package fr.geoffreyCoulaud.pinryReborn.adapters.persistence.repositories

import fr.geoffreyCoulaud.pinryReborn.adapters.persistence.mappers.UserModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.adapters.persistence.mappers.UserModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.adapters.persistence.models.SqliteUserModel
import fr.geoffreyCoulaud.pinryReborn.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.domain.repositories.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class SqliteUserRepository : SqliteBaseRepository<SqliteUserModel>(SqliteUserModel::class), UserRepository {

    override fun findUser(id: UUID): User? = findById(id)?.toDomain()

    override fun saveUser(user: User): User = saveAndReturn(user.toModel()).toDomain()

    override fun deleteUser(user: User) {
        deleteById(user.id)
    }
}
