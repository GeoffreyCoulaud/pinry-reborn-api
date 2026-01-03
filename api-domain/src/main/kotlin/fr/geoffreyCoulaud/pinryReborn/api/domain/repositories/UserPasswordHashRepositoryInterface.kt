package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.HashedPassword
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User

interface UserPasswordHashRepositoryInterface {
    fun findUserPasswordHash(user: User): HashedPassword?

    fun saveUserPasswordHash(
        user: User,
        hashedPassword: HashedPassword,
    ): HashedPassword
}
