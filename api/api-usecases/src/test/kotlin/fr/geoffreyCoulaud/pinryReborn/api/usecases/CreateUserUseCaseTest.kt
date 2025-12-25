package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserRepository
import fr.geoffreyCoulaud.pinryReborn.api.utilities.BaseTest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class CreateUserUseCaseTest : BaseTest() {
    private val userRepository = mockk<UserRepository>()
    private val useCase = CreateUserUseCase(userRepository = userRepository)

    @Test
    fun `When creating a user, then should succeed`() {
        // Given
        val user = User(name = "John Doe")
        every { userRepository.saveUser(any()) } answers { args[0] as User }

        // When
        // Then
        assertDoesNotThrow {
            useCase.execute(user)
        }
    }
}
