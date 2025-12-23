package fr.geoffreyCoulaud.pinryReborn.application

import fr.geoffreyCoulaud.pinryReborn.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.domain.repositories.UserRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CreateUserUseCase(
    private val userRepository: UserRepository,
) {
    fun execute(user: User): User = userRepository.saveUser(user)
}
