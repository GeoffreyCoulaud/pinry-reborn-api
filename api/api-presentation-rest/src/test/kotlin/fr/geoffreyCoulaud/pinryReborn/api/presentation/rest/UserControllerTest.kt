package fr.geoffreyCoulaud.pinryReborn.api.presentation.rest

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.controllers.UserController
import fr.geoffreyCoulaud.pinryReborn.api.usecases.CreateUserUseCase
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import io.mockk.every
import io.mockk.mockk
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

@QuarkusTest
@TestHTTPEndpoint(UserController::class)
class UserControllerTest {
    private val createUserUseCase: CreateUserUseCase = mockk()

    @Test
    fun `When creating a user succeeds, then should return 200 and UserOutputDto`() {
        val userName = createRandomString()
        val userId = randomUUID()
        every { createUserUseCase.execute(any()) } returns User(id = userId, name = userName)

        Given {
            contentType(JSON)
            body("""{"name":"$userName"}""")
        } When {
            post("/api/v1/users")
        } Then {
            statusCode(200)
            contentType(JSON)
            body(equalTo("""{"id":"$userId","name":"$userName"}"""))
        }
    }
}
