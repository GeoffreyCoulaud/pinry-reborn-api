package fr.geoffreyCoulaud.pinryReborn.api.application

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test

@QuarkusTest
class UserCreationIntegrationTest : IntegrationTest() {

    // ==================== Simple Scenarios ====================

    @Test
    fun `creating a user returns the created user`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"name": "testuser"}""")
            .`when`()
            .post("/api/v1/users")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo("testuser"))
    }

    @Test
    fun `creating a user with different name returns the created user`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"name": "another_user"}""")
            .`when`()
            .post("/api/v1/users")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo("another_user"))
    }

    // ==================== Complex Scenarios ====================

    @Test
    fun `creating two different users succeeds`() {
        // Create first user
        given()
            .contentType(ContentType.JSON)
            .body("""{"name": "user1"}""")
            .`when`()
            .post("/api/v1/users")
            .then()
            .statusCode(200)
            .body("name", equalTo("user1"))

        // Create second user with different name
        given()
            .contentType(ContentType.JSON)
            .body("""{"name": "user2"}""")
            .`when`()
            .post("/api/v1/users")
            .then()
            .statusCode(200)
            .body("name", equalTo("user2"))
    }

    @Test
    fun `creating a user with duplicate name fails`() {
        // Create user first time
        given()
            .contentType(ContentType.JSON)
            .body("""{"name": "duplicate_user"}""")
            .`when`()
            .post("/api/v1/users")
            .then()
            .statusCode(200)
            .body("name", equalTo("duplicate_user"))

        // Try to create user with same name
        given()
            .contentType(ContentType.JSON)
            .body("""{"name": "duplicate_user"}""")
            .`when`()
            .post("/api/v1/users")
            .then()
            .statusCode(500)  // UsernameAlreadyTakenError causes 500
    }

    @Test
    fun `creating a user with special characters in name succeeds`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"name": "user_with-special.chars123"}""")
            .`when`()
            .post("/api/v1/users")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo("user_with-special.chars123"))
    }

    @Test
    fun `creating a user with unicode name succeeds`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"name": "用户名"}""")
            .`when`()
            .post("/api/v1/users")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", equalTo("用户名"))
    }
}
