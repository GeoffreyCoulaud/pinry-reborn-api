package fr.geoffreyCoulaud.pinryReborn.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.domain.entities.User
import java.util.UUID

interface UserRepository {
    fun findUser(id: UUID): User?

    fun saveUser(user: User): User

    fun deleteUser(user: User): Unit
}
