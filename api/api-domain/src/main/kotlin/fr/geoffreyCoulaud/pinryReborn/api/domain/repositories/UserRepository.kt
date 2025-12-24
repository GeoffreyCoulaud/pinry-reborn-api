package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import java.util.UUID

interface UserRepository {
    fun findUser(id: UUID): User?

    fun saveUser(user: User): User

    fun deleteUser(user: User)
}
