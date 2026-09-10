package fr.geoffreyCoulaud.pinryReborn.api.application

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.nullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The shipped `api.cors.origins` names nobody, the proxy making the web application same origin
 * (`docs/adr/0026-one-session-two-transports.md`). A profile is the only way to reach that value.
 */
@QuarkusTest
@TestProfile(ProductionCorsTestProfile::class)
class ProductionCorsDefaultIntegrationTest {

    @Test
    fun `Given the shipped configuration, Then it names no allowed origin`() {
        // Given / When
        val declared = ProductionProperties[ProductionCorsTestProfile.ORIGINS_KEY]

        // Then
        assertEquals("", declared, "The deployment ships an allowed origin, and the proxy needs none")
    }

    @Test
    fun `Given the shipped configuration, Then a preflight from a named origin is refused`() {
        // Given / When: the origin the shipped default used to carry, which is now nobody's
        given()
            .header("Origin", FORMER_DEVELOPMENT_ORIGIN)
            .header("Access-Control-Request-Method", "POST")
            .`when`()
            .options("/api/v1/sessions")
            // Then
            .then()
            .header("Access-Control-Allow-Origin", nullValue())
    }

    @Test
    fun `Given the shipped configuration, Then a preflight from any other origin is refused`() {
        // Given / When
        given()
            .header("Origin", ARBITRARY_ORIGIN)
            .header("Access-Control-Request-Method", "POST")
            .`when`()
            .options("/api/v1/sessions")
            // Then
            .then()
            .header("Access-Control-Allow-Origin", nullValue())
    }

    private companion object {
        const val FORMER_DEVELOPMENT_ORIGIN = "http://localhost:5173"
        const val ARBITRARY_ORIGIN = "https://evil.test"
    }
}
