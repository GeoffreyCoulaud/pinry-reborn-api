package fr.geoffreyCoulaud.pinryReborn.api.application

import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinCreator
import fr.geoffreyCoulaud.pinryReborn.api.usecases.UserCreator
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class PinTaggingIntegrationTest : IntegrationTest() {

    @Inject
    lateinit var userCreator: UserCreator

    @Inject
    lateinit var pinCreator: PinCreator

    @Test
    fun `setting tags returns 200 with updated tags`() {
        val username = "taguser"
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
            .contentType(ContentType.JSON)
            .body("""{"tags": ["nature", "landscape"]}""")
            .`when`()
            .put("/api/v1/pins/${pin.id}/tags")
            .then()
            .statusCode(200)
            .body("id", equalTo(pin.id.toString()))
            .body("tags", hasSize<Any>(2))
            .body("tags.name", containsInAnyOrder("nature", "landscape"))
    }

    @Test
    fun `setting tags replaces existing tags`() {
        val username = "replaceuser"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)

        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/page",
            sourceMediaUrl = "https://example.com/image.jpg",
            description = "My pin",
            tags = listOf("oldtag1", "oldtag2")
        )

        given()
            .auth().preemptive().basic(username, password)
            .contentType(ContentType.JSON)
            .body("""{"tags": ["newtag"]}""")
            .`when`()
            .put("/api/v1/pins/${pin.id}/tags")
            .then()
            .statusCode(200)
            .body("tags", hasSize<Any>(1))
            .body("tags[0].name", equalTo("newtag"))
    }

    @Test
    fun `setting empty tags clears all tags`() {
        val username = "clearuser"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)

        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/page",
            sourceMediaUrl = "https://example.com/image.jpg",
            description = "My pin",
            tags = listOf("tag1", "tag2")
        )

        given()
            .auth().preemptive().basic(username, password)
            .contentType(ContentType.JSON)
            .body("""{"tags": []}""")
            .`when`()
            .put("/api/v1/pins/${pin.id}/tags")
            .then()
            .statusCode(200)
            .body("tags", emptyIterable<Any>())
    }

    @Test
    fun `setting tags on another user's pin returns 403`() {
        val user1 = userCreator.createUserWithPassword("owner", "password123")
        val user2 = userCreator.createUserWithPassword("attacker", "password456")

        val pin = pinCreator.createPin(
            author = user1,
            sourceContextUrl = "https://example.com/page",
            sourceMediaUrl = "https://example.com/image.jpg",
            description = "Owner's pin",
            tags = emptyList()
        )

        given()
            .auth().preemptive().basic("attacker", "password456")
            .contentType(ContentType.JSON)
            .body("""{"tags": ["hacked"]}""")
            .`when`()
            .put("/api/v1/pins/${pin.id}/tags")
            .then()
            .statusCode(403)
    }

    @Test
    fun `setting tags on non-existent pin returns 404`() {
        val username = "notfounduser"
        val password = "password123"
        userCreator.createUserWithPassword(username, password)

        val nonExistentPinId = UUID.randomUUID()

        given()
            .auth().preemptive().basic(username, password)
            .contentType(ContentType.JSON)
            .body("""{"tags": ["tag"]}""")
            .`when`()
            .put("/api/v1/pins/$nonExistentPinId/tags")
            .then()
            .statusCode(404)
    }

    @Test
    fun `unauthenticated request returns 401`() {
        val user = userCreator.createUserWithPassword("unauthuser", "password123")

        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/page",
            sourceMediaUrl = "https://example.com/image.jpg",
            description = "My pin",
            tags = emptyList()
        )

        given()
            .contentType(ContentType.JSON)
            .body("""{"tags": ["tag"]}""")
            .`when`()
            .put("/api/v1/pins/${pin.id}/tags")
            .then()
            .statusCode(401)
    }
}
