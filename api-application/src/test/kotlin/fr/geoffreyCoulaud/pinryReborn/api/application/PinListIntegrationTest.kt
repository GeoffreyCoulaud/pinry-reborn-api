package fr.geoffreyCoulaud.pinryReborn.api.application

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinCreator
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinGetter
import fr.geoffreyCoulaud.pinryReborn.api.usecases.UserCreator
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test

@QuarkusTest
class PinListIntegrationTest : IntegrationTest() {
    @Inject
    lateinit var userCreator: UserCreator

    @Inject
    lateinit var pinCreator: PinCreator

    // ==================== Helpers ====================

    private val defaultPassword = "password123"

    private fun createPinsForUser(
        user: User,
        count: Int,
    ): List<String> =
        (1..count).map { i ->
            // Small delay to ensure unique timestamps for deterministic ordering
            Thread.sleep(2)
            pinCreator
                .createPin(
                    author = user,
                    sourceContextUrl = "https://example.com/page$i",
                    sourceMediaUrl = "https://example.com/image$i.jpg",
                    description = "Pin $i",
                    tags = emptyList(),
                ).id
                .toString()
        }

    private fun RequestSpecification.authenticatedAs(
        username: String,
        password: String = defaultPassword,
    ): RequestSpecification = auth().preemptive().basic(username, password)

    @Test
    fun `getting pins returns pins with correct structure`() {
        val username = "structuser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        createPinsForUser(user, 1)

        given()
            .authenticatedAs(username)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins[0].id", notNullValue())
            .body("pins[0].authorId", notNullValue())
            .body("pins[0].sourceContextUrl", notNullValue())
            .body("pins[0].sourceMediaUrl", notNullValue())
            .body("pins[0].description", notNullValue())
            .body("pins[0].tags", notNullValue())
    }

    @Test
    fun `getting pins with pageSize returns limited results`() {
        val username = "pagesizeuser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        createPinsForUser(user, 5)

        given()
            .authenticatedAs(username)
            .queryParam("pageSize", 2)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))
    }

    @Test
    fun `getting pins with pageSize larger than total returns all`() {
        val username = "largepageuser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        createPinsForUser(user, 3)

        given()
            .authenticatedAs(username)
            .queryParam("pageSize", 10)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(3))
    }

    @Test
    fun `getting pins with pageSize coerced to max server limit`() {
        val username = "maxpageuser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        val userPinCount = PinGetter.MAX_PAGE_SIZE + 10

        createPinsForUser(user, userPinCount)

        given()
            .authenticatedAs(username)
            .queryParam("pageSize", userPinCount)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(PinGetter.MAX_PAGE_SIZE))
    }

    @Test
    fun `getting first page includes nextCursor when more pages exist`() {
        val username = "nextpageuser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        createPinsForUser(user, 5)

        given()
            .authenticatedAs(username)
            .queryParam("pageSize", 2)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))
            .body("pagination.previousCursor", notNullValue())
            .body("pagination.nextCursor", nullValue())
    }

    @Test
    fun `getting last page has no nextCursor`() {
        val username = "lastpageuser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        val pinIds = createPinsForUser(user, 3)

        given()
            .authenticatedAs(username)
            .queryParam("pageSize", 2)
            .queryParam("cursor", pinIds[1])
            .queryParam("direction", "FORWARD")
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(1))
            .body("pagination.nextCursor", nullValue())
            .body("pagination.previousCursor", notNullValue())
    }

    @Test
    fun `getting next page with cursor returns pins after cursor`() {
        val username = "cursoruser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        val pinIds = createPinsForUser(user, 5)

        given()
            .authenticatedAs(username)
            .queryParam("pageSize", 2)
            .queryParam("cursor", pinIds[1])
            .queryParam("direction", "FORWARD")
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))
            .body("pins[0].id", equalTo(pinIds[2]))
            .body("pins[1].id", equalTo(pinIds[3]))
    }

    @Test
    fun `getting previous page returns pins before cursor`() {
        val username = "prevpageuser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        val pinIds = createPinsForUser(user, 5)

        given()
            .authenticatedAs(username)
            .queryParam("pageSize", 2)
            .queryParam("cursor", pinIds[2])
            .queryParam("direction", "BACKWARD")
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))
            .body("pins[0].id", equalTo(pinIds[0]))
            .body("pins[1].id", equalTo(pinIds[1]))
    }

    @Test
    fun `getting pins with sort parameter orders results`() {
        val username = "sortuser"
        val user = userCreator.createUserWithPassword(username, defaultPassword)
        val pinIds = createPinsForUser(user, 3)

        // Ascending sort (oldest first)
        given()
            .authenticatedAs(username)
            .queryParam("sort", "CREATED_AT_ASC")
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins[0].id", equalTo(pinIds[0]))
            .body("pins[2].id", equalTo(pinIds[2]))

        // Descending sort (newest first)
        given()
            .authenticatedAs(username)
            .queryParam("sort", "CREATED_AT_DESC")
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins[0].id", equalTo(pinIds[2]))
            .body("pins[2].id", equalTo(pinIds[0]))
    }

    @Test
    fun `getting pins with no pins returns empty pagination`() {
        val username = "emptypagiuser"
        userCreator.createUserWithPassword(username, defaultPassword)

        given()
            .authenticatedAs(username)
            .queryParam("pageSize", 10)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", emptyIterable<Any>())
            .body("pagination.nextCursor", nullValue())
            .body("pagination.previousCursor", nullValue())
    }

    // ======= User isolation tests =======

    @Test
    fun `getting pins returns only pins for the requesting user`() {
        val username1 = "user1"
        val username2 = "user2"
        val user1 = userCreator.createUserWithPassword(username1, defaultPassword)
        val user2 = userCreator.createUserWithPassword(username2, defaultPassword)

        createPinsForUser(user1, 2)
        createPinsForUser(user2, 1)

        given()
            .authenticatedAs(username1)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(2))

        given()
            .authenticatedAs(username2)
            .`when`()
            .get("/api/v1/pins")
            .then()
            .statusCode(200)
            .body("pins", hasSize<Any>(1))
    }
}
