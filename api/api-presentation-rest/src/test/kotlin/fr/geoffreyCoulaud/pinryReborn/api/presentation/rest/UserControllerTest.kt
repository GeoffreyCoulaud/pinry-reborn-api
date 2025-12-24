package fr.geoffreyCoulaud.pinryReborn.api.presentation.rest

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.usecases.CreateUserUseCase
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import io.mockk.every
import io.mockk.mockk
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class UserControllerTest {
    private val createUserUseCase: CreateUserUseCase = mockk()

    @Test
    fun `When creating a user succeeds, then should return 200 and UserOutputDto`() {
        val userName = createRandomString()
        val userId = randomUUID()
        every { createUserUseCase.execute(any()) } returns User(id = userId, name = userName)

        Given {
            contentType("application/json")
            body("""{"name":"$userName"}""")
        } When {
            post("/api/v1/users")
        } Then {
            statusCode(200)
            body("id", equalTo(userId))
            body("name", equalTo(userName))
        }
    }
}
