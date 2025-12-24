package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepository
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.mappers.UserModelMapper.toModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.SqliteUserModel
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class SqliteUserRepository :
    SqliteBaseRepository<SqliteUserModel>(SqliteUserModel::class),
    UserRepository {
    override fun findUser(id: UUID): User? = findById(id)?.toDomain()

    override fun saveUser(user: User): User = saveAndReturn(user.toModel()).toDomain()

    override fun deleteUser(user: User) {
        deleteById(user.id)
    }
}
