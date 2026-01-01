package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.controllers.UserController
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.UserInputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.UserOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.usecases.UserCreator
import fr.geoffreyCoulaud.pinryReborn.api.utilities.BaseTest
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import io.mockk.every
import io.mockk.mockk
import org.jboss.resteasy.reactive.RestResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class UserControllerTest : BaseTest() {
    private val userCreator: UserCreator = mockk()
    private val controller = UserController(userCreator = userCreator)

    @Test
    fun `When creating a user succeeds, then should return UserOutputDto`() {
        // Given
        val user = User(id = randomUUID(), name = createRandomString())
        every { userCreator.createUser(any()) } returns user

        // When
        val response: RestResponse<UserOutputDto> = controller.createUser(UserInputDto(name = user.name))

        // Then
        assertNotNull(response)
        assertEquals(RestResponse.Status.OK.statusCode, response.status)
        assertNotNull(response.entity)
        assertEquals(user.id, response.entity?.id)
        assertEquals(user.name, response.entity?.name)
    }
}
