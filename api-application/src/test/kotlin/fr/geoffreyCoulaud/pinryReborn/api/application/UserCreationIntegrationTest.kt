package fr.geoffreyCoulaud.pinryReborn.api.application

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test

@QuarkusTest
class UserCreationIntegrationTest : IntegrationTest() {
    @Test
    fun `creating a user returns the created user - first run`() {
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
}
