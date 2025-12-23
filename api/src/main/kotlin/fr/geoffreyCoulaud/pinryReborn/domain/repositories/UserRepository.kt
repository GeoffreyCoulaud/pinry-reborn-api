package fr.geoffreyCoulaud.pinryReborn.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.domain.entities.User
import java.util.UUID

interface UserRepository {
    fun find(id: UUID): User?

    fun save(user: User): User

    fun delete(id: UUID)
}
