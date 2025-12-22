package fr.geoffreyCoulaud.pinryReborn.api.application.adapters

import fr.geoffreyCoulaud.pinryReborn.api.application.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.application.interfaces.UserRepository

class DummyUserRepository : UserRepository {
    override fun createUser(name: String): User = User(name = name)
}
