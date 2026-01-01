package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserPasswordRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.UsernameAlreadyTakenError
import fr.geoffreyCoulaud.pinryReborn.api.utilities.BaseTest
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class UserCreatorTest : BaseTest() {
    private val userRepository = mockk<UserRepositoryInterface>()
    private val userPasswordRepository = mockk<UserPasswordRepositoryInterface>()
    private val useCase =
        UserCreator(
            userRepository = userRepository,
            userPasswordRepository = userPasswordRepository,
        )

    @Test
    fun `When creating a user, then should succeed`() {
        // Given
        val name = "John Doe"
        every { userRepository.findUserByName(any()) } returns null
        every { userRepository.saveUser(any()) } answers { firstArg() }

        // When
        // Then
        assertDoesNotThrow {
            useCase.createUser(name)
        }
    }

    @Test
    fun `When creating a user with an already used name, then should throw`() {
        // Given
        val name = "John Doe"
        every { userRepository.findUserByName(name) } returns mockk(relaxed = true, name = name)

        // When,Then
        assertThrows<UsernameAlreadyTakenError> {
            useCase.createUser(name)
        }
    }

    @Test
    fun `When creating a user with password, then should succeed`() {
        // Given
        val name = "John Doe"
        val password = createRandomString()
        every { userRepository.findUserByName(any()) } returns null
        every { userRepository.saveUser(any()) } answers { firstArg() }
        every { userPasswordRepository.saveUserPassword(any(), any()) } answers { secondArg() }

        // When, then
        assertDoesNotThrow {
            useCase.createUserWithPassword(name = name, password = password)
        }
    }
}
