package fr.geoffreyCoulaud.pinryReborn.adapters.persistence.repositories

import fr.geoffreyCoulaud.pinryReborn.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.domain.repositories.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class SqliteUserRepository : UserRepository {
    override fun find(id: UUID): User? {
        TODO("Not yet implemented")
    }

    override fun save(user: User): User {
        TODO("Not yet implemented")
    }

    override fun delete(id: UUID) {
        TODO("Not yet implemented")
    }
}
