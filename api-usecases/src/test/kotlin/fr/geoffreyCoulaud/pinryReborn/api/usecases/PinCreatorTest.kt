package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Login.BasicAuthLogin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.LoginError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.LoginInvalidPasswordError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.LoginUserDoesNotExistError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinCreationBadLoginError
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID.randomUUID
import java.util.stream.Stream

class PinCreatorTest {
    private val pinRepository: PinRepositoryInterface = mockk()
    private val tagCreator: TagCreator = mockk()
    private val userAuthenticator: UserAuthenticator = mockk()
    private val useCase =
        PinCreator(
            userAuthenticator = userAuthenticator,
            tagCreator = tagCreator,
            pinRepository = pinRepository,
        )

    @Test
    fun `When creating a pin, then should succeed`() {
        // Given
        val user = User(randomUUID(), "John Doe")
        val login = BasicAuthLogin(userName = user.name, password = createRandomString())
        val sourceUrl = "https://example.com/article"
        val mediaUrl = "https://example.com/image.jpeg"
        val description = "some description"
        val tags = listOf("blue", "landscape", "water")
        every { userAuthenticator.authenticate(login) } returns user
        every { tagCreator.findOrCreate(any(), any()) } answers { Tag(id = randomUUID(), name = firstArg(), author = secondArg()) }
        every { pinRepository.savePin(any()) } answers { firstArg() }

        // When
        val pin =
            useCase.createPin(
                login = login,
                sourceUrl = sourceUrl,
                mediaUrl = mediaUrl,
                description = description,
                tags = tags,
            )

        // Then
        assertEquals(user, pin.author)
        assertEquals(sourceUrl, pin.sourceUrl)
        assertEquals(mediaUrl, pin.mediaUrl)
        assertEquals(description, pin.description)
        assertEquals(tags.toSet(), pin.tags.map { it.name }.toSet())
    }

    @ParameterizedTest
    @MethodSource("loginError_source")
    fun `When creating a pin with a bad login, then should throw`(loginError: LoginError) {
        // Given
        val login = BasicAuthLogin(userName = createRandomString(), password = createRandomString())
        every { userAuthenticator.authenticate(login) } throws loginError

        // When, Then
        assertThrows<PinCreationBadLoginError> {
            useCase.createPin(
                login = login,
                sourceUrl = createRandomString(),
                mediaUrl = createRandomString(),
                description = createRandomString(),
                tags = List(3) { createRandomString() },
            )
        }
    }

    companion object {
        @JvmStatic
        fun loginError_source(): Stream<Arguments> =
            Stream.of(
                Arguments.of(LoginUserDoesNotExistError()),
                Arguments.of(LoginInvalidPasswordError()),
            )
    }
}
