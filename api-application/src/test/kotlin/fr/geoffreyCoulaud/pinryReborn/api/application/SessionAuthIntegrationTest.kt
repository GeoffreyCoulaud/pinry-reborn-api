package fr.geoffreyCoulaud.pinryReborn.api.application

import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.SessionTokenModel
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import io.ebean.DB
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Instant

@QuarkusTest
// The app under test runs the real SystemClock; these read the wall clock to keep fixture instants consistent with it.
@Suppress("WallClockRead")
class SessionAuthIntegrationTest : IntegrationTest() {
    private val iso8601Utc = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z"

    private fun login(name: String, password: String = DEFAULT_PASSWORD, rememberMe: Boolean? = null) =
        given().contentType(ContentType.JSON)
            .body(buildMap<String, Any> {
                put("name", name); put("password", password)
                if (rememberMe != null) put("rememberMe", rememberMe)
            })
            .post("/api/v1/sessions")

    @Test
    fun `Given valid credentials, Then POST sessions returns 201 with a token and ISO-8601 UTC metadata`() {
        val name = createRandomString()
        userCreator.createUserWithPassword(name, DEFAULT_PASSWORD)
        login(name)
            .then().statusCode(201)
            .header("Cache-Control", "no-store")
            .body("token", notNullValue())
            .body("expiresAt", matchesPattern(iso8601Utc))
            .body("renewAfter", matchesPattern(iso8601Utc))
    }

    @Test
    fun `Given a bad password, Then POST sessions returns 401 AUTHENTICATION_FAILED`() {
        val name = createRandomString()
        userCreator.createUserWithPassword(name, DEFAULT_PASSWORD)
        login(name, password = "wrong-password")
            .then().statusCode(401).body("code", org.hamcrest.Matchers.equalTo("AUTHENTICATION_FAILED"))
    }

    @Test
    fun `Given an unknown user, Then POST sessions returns 401 AUTHENTICATION_FAILED`() {
        login(createRandomString())
            .then().statusCode(401).body("code", org.hamcrest.Matchers.equalTo("AUTHENTICATION_FAILED"))
    }

    @Test
    fun `Given a valid token, Then GET me returns the caller`() {
        val auth = createAuthenticatedUser()
        given().authenticatedAs(auth).get("/api/v1/me")
            .then().statusCode(200).body("name", org.hamcrest.Matchers.equalTo(auth.user.name))
    }

    @Test
    fun `Given no token, Then GET me returns 401 with a Bearer challenge`() {
        given().get("/api/v1/me")
            .then().statusCode(401).header("WWW-Authenticate", "Bearer")
    }

    @Test
    fun `Given a garbage token, Then GET me returns 401 AUTHENTICATION_FAILED`() {
        given().header("Authorization", "Bearer not-a-real-token").get("/api/v1/me")
            .then().statusCode(401).body("code", org.hamcrest.Matchers.equalTo("AUTHENTICATION_FAILED"))
    }

    @Test
    fun `Given a token, Then GET sessions current returns expiry metadata without a token`() {
        val auth = createAuthenticatedUser(rememberMe = true)
        given().authenticatedAs(auth).get("/api/v1/sessions/current")
            .then().statusCode(200)
            .body("expiresAt", matchesPattern(iso8601Utc))
            .body("renewAfter", matchesPattern(iso8601Utc))
            .body("persistent", org.hamcrest.Matchers.equalTo(true))
            .body("$", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasKey("token")))
    }

    @Test
    fun `Given a token, Then renew returns a new token and the old one is rejected`() {
        val auth = createAuthenticatedUser()
        val newToken = given().authenticatedAs(auth).post("/api/v1/sessions/current/renew")
            .then().statusCode(200).header("Cache-Control", "no-store").extract().path<String>("token")
        assertNotNull(newToken)
        // Old token now rejected:
        given().authenticatedAs(auth).get("/api/v1/me").then().statusCode(401)
        // New token works:
        given().header("Authorization", "Bearer $newToken").get("/api/v1/me").then().statusCode(200)
    }

    @Test
    fun `Given a token, Then DELETE sessions current logs it out`() {
        val auth = createAuthenticatedUser()
        given().authenticatedAs(auth).delete("/api/v1/sessions/current").then().statusCode(204)
        given().authenticatedAs(auth).get("/api/v1/me").then().statusCode(401)
    }

    @Test
    fun `Given two sessions for one user, Then DELETE sessions revokes them all`() {
        val name = createRandomString()
        userCreator.createUserWithPassword(name, DEFAULT_PASSWORD)
        val first = login(name).then().statusCode(201).extract().path<String>("token")
        val second = login(name).then().statusCode(201).extract().path<String>("token")

        given().header("Authorization", "Bearer $first").delete("/api/v1/sessions").then().statusCode(204)

        given().header("Authorization", "Bearer $first").get("/api/v1/me")
            .then().statusCode(401).body("code", org.hamcrest.Matchers.equalTo("AUTHENTICATION_FAILED"))
        given().header("Authorization", "Bearer $second").get("/api/v1/me")
            .then().statusCode(401).body("code", org.hamcrest.Matchers.equalTo("AUTHENTICATION_FAILED"))
    }

    @Test
    fun `Given an expired token, Then it is rejected with 401 SESSION_EXPIRED`() {
        // Spec §12: a dedicated SessionExpiredException subtype was tried in Task 9 but never routed
        // through the JAX-RS exception-mapper chain at runtime. AuthenticationFailedExceptionMapper now
        // inspects the exception's cause instead, distinguishing an expired token from an invalid one
        // while reusing the (proven to route) AuthenticationFailedException path.
        val auth = createAuthenticatedUser()
        // Age the single session-token row directly via Ebean (deterministic, no clock mocking).
        val model = DB.getDefault().find(SessionTokenModel::class.java).findList().single()
        model.expiresAt = Instant.now().minusSeconds(60)
        DB.getDefault().save(model)

        given().authenticatedAs(auth).get("/api/v1/me")
            .then().statusCode(401).body("code", org.hamcrest.Matchers.equalTo("SESSION_EXPIRED"))
    }

    // What the framework refuses before a use case runs shares the problem format: one case per row of
    // docs/specs/2026-09-05-p2-debt-elimination.md section 4.5.

    @Test
    fun `Given a body that is not JSON, Then POST sessions returns 400 MALFORMED_BODY`() {
        given().contentType(ContentType.JSON).body("{not json").post("/api/v1/sessions")
            .then().statusCode(400).contentType(PROBLEM_JSON).body("code", equalTo("MALFORMED_BODY"))
    }

    @Test
    fun `Given a path no resource serves, Then the response is 404 UNKNOWN_ROUTE`() {
        given().get("/api/v1/nowhere")
            .then().statusCode(404).contentType(PROBLEM_JSON).body("code", equalTo("UNKNOWN_ROUTE"))
    }

    @Test
    fun `Given a path value the framework cannot read, Then the response is 404 UNKNOWN_ROUTE saying so`() {
        // Given: JAX-RS 3.2 makes a path parameter it cannot convert a NotFoundException carrying the cause
        val auth = createAuthenticatedUser()
        given().authenticatedAs(auth).get("/api/v1/pins/not-a-uuid")
            .then().statusCode(404).contentType(PROBLEM_JSON).body("code", equalTo("UNKNOWN_ROUTE"))
            .body("detail", equalTo("A path or query value could not be read"))
    }

    @Test
    fun `Given a method the path does not serve, Then the response is 405 METHOD_NOT_ALLOWED`() {
        given().delete("/api/v1/pins")
            .then().statusCode(405).contentType(PROBLEM_JSON).body("code", equalTo("METHOD_NOT_ALLOWED"))
    }

    @Test
    fun `Given a content type the route does not read, Then the response is 415 UNSUPPORTED_MEDIA_TYPE`() {
        given().contentType(ContentType.TEXT).body("name=x").post("/api/v1/sessions")
            .then().statusCode(415).contentType(PROBLEM_JSON).body("code", equalTo("UNSUPPORTED_MEDIA_TYPE"))
    }

    @Test
    fun `Given an Accept the route cannot produce, Then the response is 406 NOT_ACCEPTABLE`() {
        given().accept(ContentType.TEXT).get("/test/failures/json-only")
            .then().statusCode(406).contentType(PROBLEM_JSON).body("code", equalTo("NOT_ACCEPTABLE"))
    }

    @Test
    fun `Given a WebApplicationException no mapper names, Then the response keeps its status under HTTP_ERROR`() {
        given().get("/test/failures/service-unavailable")
            .then().statusCode(503).contentType(PROBLEM_JSON).body("code", equalTo("HTTP_ERROR"))
    }

    @Test
    fun `Given an unmapped exception, Then the response is 500 INTERNAL_ERROR and tells nothing of it`() {
        given().get("/test/failures/illegal-state")
            .then().statusCode(500).contentType(PROBLEM_JSON)
            .body("code", equalTo("INTERNAL_ERROR")).body("detail", nullValue())
            .body(not(containsString(TestFailuresResource.MARKER)))
    }

    @Test
    fun `Given an IOException, Then the response is 500 INTERNAL_ERROR and tells nothing of it`() {
        given().get("/test/failures/io")
            .then().statusCode(500).contentType(PROBLEM_JSON)
            .body("code", equalTo("INTERNAL_ERROR")).body("detail", nullValue())
            .body(not(containsString(TestFailuresResource.MARKER)))
    }

    private companion object {
        const val PROBLEM_JSON = "application/problem+json"
    }
}
