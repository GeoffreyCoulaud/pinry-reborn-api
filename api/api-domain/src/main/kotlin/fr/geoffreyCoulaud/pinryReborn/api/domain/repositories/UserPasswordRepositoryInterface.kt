package fr.geoffreyCoulaud.pinryReborn.api.domain.repositories

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.HashedPassword
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User

interface UserPasswordRepositoryInterface {
    fun findUserPasswordHash(user: User): HashedPassword?
}
