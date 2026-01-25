package fr.geoffreyCoulaud.pinryReborn.api.application

import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinCreator
import fr.geoffreyCoulaud.pinryReborn.api.usecases.UserCreator
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.emptyIterable
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class PinRetrievalIntegrationTest : IntegrationTest() {

    @Inject
    lateinit var userCreator: UserCreator

    @Inject
    lateinit var pinCreator: PinCreator

    // ==================== Simple Scenarios ====================

    @Test
    fun `retrieving own pin returns the pin`() {
        val username = "retrieveuser"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)

        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/page",
            sourceMediaUrl = "https://example.com/image.jpg",
            description = "My pin",
            tags = emptyList()
        )

        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(200)
            .body("id", equalTo(pin.id.toString()))
            .body("author.name", equalTo(username))
            .body("sourceContextUrl", equalTo("https://example.com/page"))
            .body("sourceMediaUrl", equalTo("https://example.com/image.jpg"))
            .body("description", equalTo("My pin"))
            .body("tags", emptyIterable<Any>())
    }

    @Test
    fun `retrieving pin returns correct author information`() {
        val username = "authorinfouser"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)

        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://author.com",
            sourceMediaUrl = "https://author.com/img.jpg",
            description = "Author test",
            tags = emptyList()
        )

        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(200)
            .body("author.id", notNullValue())
            .body("author.name", equalTo(username))
    }

    // ==================== Complex Scenarios ====================

    @Test
    fun `retrieving non-existent pin returns 404`() {
        val username = "notfounduser"
        val password = "password123"
        userCreator.createUserWithPassword(username, password)

        val nonExistentPinId = UUID.randomUUID()

        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/$nonExistentPinId")
            .then()
            .statusCode(404)
    }

    @Test
    fun `retrieving pin without authentication returns 401`() {
        val user = userCreator.createUserWithPassword("unauthuser", "password123")

        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://unauth.com",
            sourceMediaUrl = "https://unauth.com/img.jpg",
            description = "Unauth test",
            tags = emptyList()
        )

        given()
            .`when`()
            .get("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(401)
    }

    @Test
    fun `retrieving another user's pin returns 403 forbidden`() {
        // Create two users
        val user1 = userCreator.createUserWithPassword("owner", "password123")
        val user2 = userCreator.createUserWithPassword("reader", "password456")

        // Create pin owned by user1
        val pin = pinCreator.createPin(
            author = user1,
            sourceContextUrl = "https://owned.com",
            sourceMediaUrl = "https://owned.com/img.jpg",
            description = "User1's pin",
            tags = emptyList()
        )

        // Try to retrieve user1's pin as user2
        given()
            .auth().preemptive().basic("reader", "password456")
            .`when`()
            .get("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(403)
    }

    @Test
    fun `user can retrieve only their own pins from multiple pins`() {
        val user1 = userCreator.createUserWithPassword("multiowner1", "password123")
        val user2 = userCreator.createUserWithPassword("multiowner2", "password456")

        // Create pin for user1
        val pin1 = pinCreator.createPin(
            author = user1,
            sourceContextUrl = "https://user1.com",
            sourceMediaUrl = "https://user1.com/img.jpg",
            description = "User1's pin",
            tags = emptyList()
        )

        // Create pin for user2
        val pin2 = pinCreator.createPin(
            author = user2,
            sourceContextUrl = "https://user2.com",
            sourceMediaUrl = "https://user2.com/img.jpg",
            description = "User2's pin",
            tags = emptyList()
        )

        // User1 can access their own pin
        given()
            .auth().preemptive().basic("multiowner1", "password123")
            .`when`()
            .get("/api/v1/pins/${pin1.id}")
            .then()
            .statusCode(200)
            .body("description", equalTo("User1's pin"))

        // User1 cannot access user2's pin
        given()
            .auth().preemptive().basic("multiowner1", "password123")
            .`when`()
            .get("/api/v1/pins/${pin2.id}")
            .then()
            .statusCode(403)

        // User2 can access their own pin
        given()
            .auth().preemptive().basic("multiowner2", "password456")
            .`when`()
            .get("/api/v1/pins/${pin2.id}")
            .then()
            .statusCode(200)
            .body("description", equalTo("User2's pin"))

        // User2 cannot access user1's pin
        given()
            .auth().preemptive().basic("multiowner2", "password456")
            .`when`()
            .get("/api/v1/pins/${pin1.id}")
            .then()
            .statusCode(403)
    }

    @Test
    fun `retrieving pin with wrong password returns 401`() {
        val username = "wrongpassretrieve"
        val password = "correctpassword"
        val user = userCreator.createUserWithPassword(username, password)

        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://wrongpass.com",
            sourceMediaUrl = "https://wrongpass.com/img.jpg",
            description = "Wrong pass test",
            tags = emptyList()
        )

        given()
            .auth().preemptive().basic(username, "wrongpassword")
            .`when`()
            .get("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(401)
    }

    @Test
    fun `user can retrieve multiple pins they created`() {
        val username = "multipinuser"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)

        val pin1 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://multi1.com",
            sourceMediaUrl = "https://multi1.com/img.jpg",
            description = "First pin",
            tags = emptyList()
        )

        val pin2 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://multi2.com",
            sourceMediaUrl = "https://multi2.com/img.jpg",
            description = "Second pin",
            tags = emptyList()
        )

        // Retrieve first pin
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/${pin1.id}")
            .then()
            .statusCode(200)
            .body("description", equalTo("First pin"))

        // Retrieve second pin
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/${pin2.id}")
            .then()
            .statusCode(200)
            .body("description", equalTo("Second pin"))
    }

    @Test
    fun `retrieving pin with invalid UUID format returns error`() {
        val username = "invaliduuiduser"
        val password = "password123"
        userCreator.createUserWithPassword(username, password)

        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/not-a-valid-uuid")
            .then()
            .statusCode(404) // Invalid UUID format results in 404
    }
}
