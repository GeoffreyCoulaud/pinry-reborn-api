package fr.geoffreyCoulaud.pinryReborn.api.application

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class GreetingTest {
    @Test
    fun testHelloEndpoint() {
        RestAssured
            .given()
            .`when`()
            .get("/hello")
            .then()
            .statusCode(200)
            .body(CoreMatchers.`is`("hello"))
    }

    @Test
    fun testGreetingEndpoint() {
        val uuid: String = UUID.randomUUID().toString()
        RestAssured
            .given()
            .pathParam("name", uuid)
            .`when`()
            .get("/hello/greeting/{name}")
            .then()
            .statusCode(200)
            .body(CoreMatchers.`is`("hello $uuid"))
    }
}
