package fr.geoffreyCoulaud.pinryReborn.api.application.interfaces

import fr.geoffreyCoulaud.pinryReborn.api.application.entities.User

interface UserRepository {
    fun createUser(name: String): User
}
