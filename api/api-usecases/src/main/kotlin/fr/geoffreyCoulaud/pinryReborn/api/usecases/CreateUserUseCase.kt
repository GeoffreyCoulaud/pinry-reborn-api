package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepositoryInterface
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CreateUserUseCase(
    private val userRepository: UserRepositoryInterface,
) {
    fun execute(user: User): User = userRepository.saveUser(user)
}
