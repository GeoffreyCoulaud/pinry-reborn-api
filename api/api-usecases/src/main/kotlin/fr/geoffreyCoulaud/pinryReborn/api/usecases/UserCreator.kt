package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.HashedPassword
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PasswordHashAlgorithm
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserPasswordRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.UsernameAlreadyTakenError
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.mindrot.jbcrypt.BCrypt

@ApplicationScoped
class UserCreator(
    private val userRepository: UserRepositoryInterface,
    private val userPasswordRepository: UserPasswordRepositoryInterface,
) {
    @Transactional
    fun createUser(user: User): User {
        // Check that the username is free
        val existingUser = userRepository.findUserByName(user.name)
        if (existingUser != null) throw UsernameAlreadyTakenError()
        // Create the user
        return userRepository.saveUser(user)
    }

    @Transactional
    fun createUserWithPassword(
        user: User,
        password: String,
    ): User {
        // Create the user as usual
        val user = createUser(user)
        // Hash and save the password
        userPasswordRepository.saveUserPassword(
            user = user,
            hashedPassword =
                HashedPassword(
                    hash = BCrypt.hashpw(password, BCrypt.gensalt()),
                    algorithm = PasswordHashAlgorithm.BCRYPT,
                ),
        )
        return user
    }
}
