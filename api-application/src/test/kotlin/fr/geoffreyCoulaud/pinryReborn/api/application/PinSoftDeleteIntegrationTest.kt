package fr.geoffreyCoulaud.pinryReborn.api.application

import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinCreator
import fr.geoffreyCoulaud.pinryReborn.api.usecases.UserCreator
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class PinSoftDeleteIntegrationTest : IntegrationTest() {

    @Inject
    lateinit var userCreator: UserCreator

    @Inject
    lateinit var pinCreator: PinCreator

    // --- Soft delete ---

    @Test
    fun `Given own pin, Then soft delete returns 204 and pin no longer in listing`() {
        // Given
        val username = "softdeluser"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "To be deleted",
            tags = emptyList(),
        )

        // When
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .delete("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(204)

        // Then - pin no longer in listing
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", emptyIterable<Any>())
    }

    @Test
    fun `Given soft-deleted pin, Then GET by id still returns it with softDeletedAt set`() {
        // Given
        val username = "getdeleted"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Still accessible",
            tags = emptyList(),
        )

        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .delete("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(204)

        // When / Then
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(200)
            .body("id", equalTo(pin.id.toString()))
            .body("softDeletedAt", notNullValue())
    }

    @Test
    fun `Given soft-deleted pin, Then pin excluded from search`() {
        // Given
        val username = "searchexclude"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Beautiful landscape painting",
            tags = emptyList(),
        )

        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .delete("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(204)

        // When / Then
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/search?q=landscape")
            .then()
            .statusCode(200)
            .body("results", emptyIterable<Any>())
    }

    @Test
    fun `Given already soft-deleted pin, Then soft delete returns 409`() {
        // Given
        val username = "doubledel"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Double delete",
            tags = emptyList(),
        )

        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .delete("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(204)

        // When / Then
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .delete("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(409)
    }

    @Test
    fun `Given pin not owned by user, Then soft delete returns 403`() {
        // Given
        val owner = userCreator.createUserWithPassword("delowner", "password123")
        userCreator.createUserWithPassword("delother", "password456")
        val pin = pinCreator.createPin(
            author = owner,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Not yours",
            tags = emptyList(),
        )

        // When / Then
        given()
            .auth().preemptive().basic("delother", "password456")
            .`when`()
            .delete("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(403)
    }

    @Test
    fun `Given unauthenticated request, Then soft delete returns 401`() {
        val user = userCreator.createUserWithPassword("delunauthuser", "password123")
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Unauth",
            tags = emptyList(),
        )

        given()
            .`when`()
            .delete("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(401)
    }

    // --- Recycle bin listing ---

    @Test
    fun `Given soft-deleted pins, Then recycle bin lists them paginated`() {
        // Given
        val username = "recyclelist"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin1 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/1",
            sourceMediaUrl = "https://example.com/img1.jpg",
            description = "Deleted 1",
            tags = emptyList(),
        )
        val pin2 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/2",
            sourceMediaUrl = "https://example.com/img2.jpg",
            description = "Deleted 2",
            tags = emptyList(),
        )

        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin1.id}")
        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin2.id}")

        // When / Then
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/recycled")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))
    }

    @Test
    fun `Given soft-deleted pins, Then default sort is most recently deleted first`() {
        // Given
        val username = "recyclesortdefault"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin1 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/1",
            sourceMediaUrl = "https://example.com/img1.jpg",
            description = "Deleted first",
            tags = emptyList(),
        )
        val pin2 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/2",
            sourceMediaUrl = "https://example.com/img2.jpg",
            description = "Deleted second",
            tags = emptyList(),
        )

        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin1.id}")
        Thread.sleep(2)
        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin2.id}")

        // When / Then - default sort should be DELETED_AT_DESC (most recently deleted first)
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/recycled")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))
            .body("pins[0].id", equalTo(pin2.id.toString()))
            .body("pins[1].id", equalTo(pin1.id.toString()))
    }

    @Test
    fun `Given soft-deleted pins, Then explicit DELETED_AT_DESC sort works`() {
        // Given
        val username = "recyclesortexplicit"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin1 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/1",
            sourceMediaUrl = "https://example.com/img1.jpg",
            description = "Deleted first",
            tags = emptyList(),
        )
        val pin2 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/2",
            sourceMediaUrl = "https://example.com/img2.jpg",
            description = "Deleted second",
            tags = emptyList(),
        )

        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin1.id}")
        Thread.sleep(2)
        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin2.id}")

        // When / Then
        given()
            .auth().preemptive().basic(username, password)
            .queryParam("sort", "DELETED_AT_DESC")
            .`when`()
            .get("/api/v1/pins/recycled")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))
            .body("pins[0].id", equalTo(pin2.id.toString()))
            .body("pins[1].id", equalTo(pin1.id.toString()))
    }

    @Test
    fun `Given soft-deleted pins, Then CREATED_AT_ASC sort still works on recycle bin`() {
        // Given
        val username = "recyclesortcreated"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin1 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/1",
            sourceMediaUrl = "https://example.com/img1.jpg",
            description = "Created first",
            tags = emptyList(),
        )
        Thread.sleep(2)
        val pin2 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/2",
            sourceMediaUrl = "https://example.com/img2.jpg",
            description = "Created second",
            tags = emptyList(),
        )

        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin2.id}")
        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin1.id}")

        // When / Then - CREATED_AT_ASC should sort by creation date regardless of deletion order
        given()
            .auth().preemptive().basic(username, password)
            .queryParam("sort", "CREATED_AT_ASC")
            .`when`()
            .get("/api/v1/pins/recycled")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))
            .body("pins[0].id", equalTo(pin1.id.toString()))
            .body("pins[1].id", equalTo(pin2.id.toString()))
    }

    // --- Restore ---

    @Test
    fun `Given soft-deleted pin, Then restore returns 200 and pin back in normal listing`() {
        // Given
        val username = "restoreuser"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "To restore",
            tags = emptyList(),
        )

        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin.id}")

        // When
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .post("/api/v1/pins/recycled/${pin.id}/restore")
            .then()
            .statusCode(200)
            .body("id", equalTo(pin.id.toString()))
            .body("softDeletedAt", nullValue())

        // Then - back in normal listing
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(1))
    }

    @Test
    fun `Given active pin, Then restore returns 409`() {
        // Given
        val username = "restoreactive"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Active",
            tags = emptyList(),
        )

        // When / Then
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .post("/api/v1/pins/recycled/${pin.id}/restore")
            .then()
            .statusCode(409)
    }

    // --- Permanent delete ---

    @Test
    fun `Given soft-deleted pin, Then permanent delete returns 204 and pin gone entirely`() {
        // Given
        val username = "permdel"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Permanent delete",
            tags = emptyList(),
        )

        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin.id}")

        // When
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .delete("/api/v1/pins/recycled/${pin.id}")
            .then()
            .statusCode(204)

        // Then - gone entirely
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/${pin.id}")
            .then()
            .statusCode(404)
    }

    @Test
    fun `Given active pin, Then permanent delete returns 409`() {
        // Given
        val username = "permdelactive"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Still active",
            tags = emptyList(),
        )

        // When / Then
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .delete("/api/v1/pins/recycled/${pin.id}")
            .then()
            .statusCode(409)
    }

    // --- Empty recycle bin ---

    @Test
    fun `Given user with soft-deleted pins, Then empty recycle bin returns 204 and all gone`() {
        // Given
        val username = "emptybin"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin1 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/1",
            sourceMediaUrl = "https://example.com/img1.jpg",
            description = "Bin 1",
            tags = emptyList(),
        )
        val pin2 = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com/2",
            sourceMediaUrl = "https://example.com/img2.jpg",
            description = "Bin 2",
            tags = emptyList(),
        )

        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin1.id}")
        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin2.id}")

        // When
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .delete("/api/v1/pins/recycled")
            .then()
            .statusCode(204)

        // Then - recycle bin empty
        given()
            .auth().preemptive().basic(username, password)
            .`when`()
            .get("/api/v1/pins/recycled")
            .then()
            .statusCode(200)
            .body("pins", emptyIterable<Any>())
    }

    // --- Tag soft-deleted pin ---

    @Test
    fun `Given soft-deleted pin, Then tagging returns 409`() {
        // Given
        val username = "tagdeleted"
        val password = "password123"
        val user = userCreator.createUserWithPassword(username, password)
        val pin = pinCreator.createPin(
            author = user,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/img.jpg",
            description = "Tag deleted",
            tags = emptyList(),
        )

        given().auth().preemptive().basic(username, password).delete("/api/v1/pins/${pin.id}")

        // When / Then
        given()
            .auth().preemptive().basic(username, password)
            .contentType(ContentType.JSON)
            .body("""{"tags": ["newtag"]}""")
            .`when`()
            .put("/api/v1/pins/${pin.id}/tags")
            .then()
            .statusCode(409)
    }
}
