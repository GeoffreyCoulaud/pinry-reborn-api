package fr.geoffreyCoulaud.pinryReborn.api.application.useCases

import fr.geoffreyCoulaud.pinryReborn.api.application.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.application.interfaces.UserRepository

class CreateUserUseCase(
    private val userRepository: UserRepository,
) {
    fun execute(name: String): User = userRepository.createUser(name)
}
