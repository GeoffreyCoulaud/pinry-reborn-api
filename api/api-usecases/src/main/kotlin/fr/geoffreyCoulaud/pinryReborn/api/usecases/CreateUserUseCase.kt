package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CreateUserUseCase(
    private val userRepository: UserRepository,
) {
    fun execute(user: User): User = userRepository.saveUser(user)
}
