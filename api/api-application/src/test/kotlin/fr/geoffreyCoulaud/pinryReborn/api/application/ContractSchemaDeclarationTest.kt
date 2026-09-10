package fr.geoffreyCoulaud.pinryReborn.api.application

import com.fasterxml.jackson.databind.JsonNode
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadStatus
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinImageStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The contract declares what the server emits: a cursor is the opaque Base64 string the wire
 * carries, and a status filled from an enum's name declares that enum's values.
 */
class ContractSchemaDeclarationTest {
    private val nullableString = setOf("string", "null")
    private val regenerate = "./gradlew :api-application:quarkusAppPartsBuild --rerun"

    private val operationsCarryingACursor =
        listOf(
            "GET /api/v1/pins",
            "GET /api/v1/pins/recycled",
            "GET /api/v1/boards/{boardId}/pins",
            "GET /api/v1/me/exports",
            "GET /api/v1/me/imports",
            "GET /api/v1/me/imports/{id}/issues",
        )

    @Test
    fun `Given the published contract, Then every cursor query parameter is declared a nullable string`() {
        // Given
        val declared = cursorParameterTypes()

        // When
        val wrong = declared.filterValues { it != nullableString }

        // Then
        assertTrue(
            declared.keys.containsAll(operationsCarryingACursor),
            "Expected a cursor query parameter on $operationsCarryingACursor, found one on ${declared.keys}.",
        )
        assertEquals(
            emptyMap<String, Set<String>>(),
            wrong,
            "These operations declare a cursor the wire never carries. Regenerate after fixing the " +
                "schema: $regenerate",
        )
    }

    @Test
    fun `Given the published contract, Then both pagination cursors are declared nullable strings`() {
        // Given
        val properties = PublishedContract.schema("PaginationOutputDto").path("properties")

        // When
        val wrong =
            listOf("previousCursor", "nextCursor")
                .associateWith { effectiveTypes(properties.path(it)) }
                .filterValues { it != nullableString }

        // Then
        assertEquals(
            emptyMap<String, Set<String>>(),
            wrong,
            "PaginationOutputDto serialises both cursors through Base64JsonSerializer, so both are " +
                "strings on the wire. Regenerate after fixing the schema: $regenerate",
        )
    }

    @Test
    fun `Given the published contract, Then the pin image status declares the values the server emits`() {
        // Given
        val status = PublishedContract.schema("PinImageStateDto").path("properties").path("status")

        // Then
        assertEquals(
            PinImageStatus.entries.map { it.name }.toSet(),
            enumeration(status),
            "PinImageStateMapper fills this field from PinImageStatus, and it is the discriminator " +
                "every tile of the grid reads. Regenerate after fixing the schema: $regenerate",
        )
    }

    @Test
    fun `Given the published contract, Then the replacement status declares the values the server emits`() {
        // Given
        val status = PublishedContract.schema("ReplacementDto").path("properties").path("status")

        // Then
        assertEquals(
            DownloadStatus.entries.map { it.name }.toSet(),
            enumeration(status),
            "PinImageStateMapper fills this field from DownloadStatus, the same field one level " +
                "down. Regenerate after fixing the schema: $regenerate",
        )
    }

    /** Every `cursor` query parameter the contract declares, keyed by the operation carrying it. */
    private fun cursorParameterTypes(): Map<String, Set<String>> =
        PublishedContract.document
            .path("paths")
            .properties()
            .flatMap { (path, operations) ->
                operations.properties().flatMap { (method, operation) ->
                    operation
                        .path("parameters")
                        .filter { it.path("name").asText() == "cursor" }
                        .map { "${method.uppercase()} $path" to effectiveTypes(it.path("schema")) }
                }
            }.toMap()

    private fun effectiveTypes(schema: JsonNode): Set<String> =
        resolve(schema) { node ->
            node.path("type").let { type ->
                if (type.isArray) type.map { it.asText() }.toSet() else setOf(type.asText())
            }
        }

    private fun enumeration(schema: JsonNode): Set<String> =
        resolve(schema) { node -> node.path("enum").map { it.asText() }.toSet() }

    /** Follows `$ref` and unions over `anyOf`, which is how SmallRye spells a nullable reference. */
    private fun resolve(schema: JsonNode, read: (JsonNode) -> Set<String>): Set<String> =
        when {
            schema.has("\$ref") ->
                resolve(PublishedContract.schema(schema.path("\$ref").asText().substringAfterLast('/')), read)
            schema.has("anyOf") -> schema.path("anyOf").flatMap { resolve(it, read) }.toSet()
            else -> read(schema)
        }
}
