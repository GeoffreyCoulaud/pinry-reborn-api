package fr.geoffreyCoulaud.pinryReborn.api.application

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File

/**
 * `info.version` is the contract's own number and never the build's (`docs/adr/0024`, decision 6):
 * asserting it merely carries a value passes on the document this replaced, where the two were equal.
 */
class ContractVersionDeclarationTest {
    private val infoVersionKey = "quarkus.smallrye-openapi.info-version"
    private val buildVersionLine = Regex("""^version\s*=\s*"([^"]+)"$""")

    @Test
    fun `Given the published contract, Then its version is declared here and is not the build's`() {
        // Given
        val declared = readProductionProperties()[infoVersionKey]
        val published = publishedContractVersion()
        val built = buildVersion()

        // Then
        assertNotNull(
            declared,
            "Expected $infoVersionKey in src/main/resources/application.properties: without it " +
                "SmallRye fills info.version from quarkus.application.version.",
        )
        assertEquals(
            declared,
            published,
            "contract/openapi.json announces a version src/main/resources/application.properties does not " +
                "declare. Regenerate it: ./gradlew :api-application:quarkusAppPartsBuild --rerun",
        )
        assertNotEquals(
            built,
            published,
            "The contract announces the build's version ($built). The two are independent: an image tag is " +
                "not a contract change and must not read as one.",
        )
    }

    private fun publishedContractVersion(): String =
        ObjectMapper()
            .readTree(File("../../contract/openapi.json"))
            .path("info")
            .path("version")
            .asText()

    /** The one place the Gradle version is written, which is what `quarkus.application.version` carries. */
    private fun buildVersion(): String =
        File("../build.gradle.kts")
            .readLines()
            .firstNotNullOfOrNull { buildVersionLine.find(it.trim())?.groupValues?.get(1) }
            ?: error("No `version = \"...\"` line in api/build.gradle.kts")

    private fun readProductionProperties(): Map<String, String> =
        File("src/main/resources/application.properties")
            .readLines()
            .map { it.trim() }
            .filterNot { it.startsWith("#") || it.isEmpty() }
            .mapNotNull { line ->
                val separator = line.indexOf('=')
                if (separator <= 0) null else line.take(separator).trim() to line.drop(separator + 1).trim()
            }
            .toMap()
}
