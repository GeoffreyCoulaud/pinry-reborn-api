package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.HashedPassword
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
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
import java.util.UUID

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
        val user = User(id = UUID.randomUUID(), name = "John Doe")
        every { userRepository.findUserByName(any()) } returns null
        every { userRepository.saveUser(any()) } answers { args[0] as User }

        // When
        // Then
        assertDoesNotThrow {
            useCase.createUser(user)
        }
    }

    @Test
    fun `When creating a user with an already used name, then should throw`() {
        // Given
        val user = User(id = UUID.randomUUID(), name = "John Doe")
        every { userRepository.findUserByName("John Doe") } returns mockk(relaxed = true)

        // When,Then
        assertThrows<UsernameAlreadyTakenError> {
            useCase.createUser(user)
        }
    }

    @Test
    fun `When creating a user with password, then should succeed`() {
        // Given
        val user = User(id = UUID.randomUUID(), name = "John Doe")
        val password = createRandomString()
        every { userRepository.findUserByName(any()) } returns null
        every { userRepository.saveUser(any()) } answers { args[0] as User }
        every { userPasswordRepository.saveUserPassword(any(), any()) } answers { args[1] as HashedPassword }

        // When, then
        assertDoesNotThrow {
            useCase.createUserWithPassword(user, password)
        }
    }
}
