package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import java.util.UUID

interface UserRepositoryInterface {
    fun findUserById(id: UUID): User?

    fun findUserByName(name: String): User?

    fun saveUser(user: User): User

    fun deleteUser(user: User)
}
