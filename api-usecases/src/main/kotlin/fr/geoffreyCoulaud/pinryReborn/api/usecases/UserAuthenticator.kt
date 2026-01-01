package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.HashedPassword
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Login
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Login.BasicAuthLogin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.PasswordHashAlgorithm
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserPasswordRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.LoginInvalidPasswordError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.LoginUserDoesNotExistError
import jakarta.enterprise.context.ApplicationScoped
import org.mindrot.jbcrypt.BCrypt

@ApplicationScoped
class UserAuthenticator(
    private val userRepository: UserRepositoryInterface,
    private val userPasswordRepository: UserPasswordRepositoryInterface,
) {
    fun authenticate(login: Login): User =
        when (login) {
            is BasicAuthLogin -> checkLogin(login)
        }

    private fun checkLogin(login: BasicAuthLogin): User {
        val user = userRepository.findUserByName(login.userName) ?: throw LoginUserDoesNotExistError()
        // If no password hash is saved for the user, login automatically
        val hash = userPasswordRepository.findUserPasswordHash(user) ?: return user
        // Check the password with the stored hash
        return user.takeIf { checkPassword(login.password, hash) } ?: throw LoginInvalidPasswordError()
    }

    private fun checkPassword(
        received: String,
        stored: HashedPassword,
    ): Boolean {
        when (stored.algorithm) {
            PasswordHashAlgorithm.BCRYPT -> return BCrypt.checkpw(received, stored.hash)
        }
    }
}
