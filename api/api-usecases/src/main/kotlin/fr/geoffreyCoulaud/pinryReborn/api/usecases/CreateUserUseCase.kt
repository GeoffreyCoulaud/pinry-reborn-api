package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.UsernameAlreadyTakenError
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CreateUserUseCase(
    private val userRepository: UserRepositoryInterface,
) {
    fun execute(user: User): User {
        // Check that the username is free
        val existingUser = userRepository.findUserByName(user.name)
        if (existingUser != null) throw UsernameAlreadyTakenError()
        // Create the user
        return userRepository.saveUser(user)
    }
}
