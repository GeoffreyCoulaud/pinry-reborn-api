package fr.geoffreyCoulaud.pinryReborn.api.application

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class GreetingIntegrationTest : IntegrationTest() {

    // ==================== Simple Scenarios ====================

    @Test
    fun `hello endpoint returns hello`() {
        given()
            .`when`()
            .get("/hello")
            .then()
            .statusCode(200)
            .body(`is`("hello"))
    }

    @Test
    fun `greeting endpoint with random uuid name returns correct greeting`() {
        val uuid: String = UUID.randomUUID().toString()
        given()
            .pathParam("name", uuid)
            .`when`()
            .get("/hello/greeting/{name}")
            .then()
            .statusCode(200)
            .body(`is`("hello $uuid"))
    }

    @Test
    fun `hello endpoint returns plain text content type`() {
        given()
            .`when`()
            .get("/hello")
            .then()
            .statusCode(200)
            .contentType("text/plain")
    }

    @Test
    fun `greeting endpoint returns plain text content type`() {
        given()
            .pathParam("name", "Test")
            .`when`()
            .get("/hello/greeting/{name}")
            .then()
            .statusCode(200)
            .contentType("text/plain")
    }
}
