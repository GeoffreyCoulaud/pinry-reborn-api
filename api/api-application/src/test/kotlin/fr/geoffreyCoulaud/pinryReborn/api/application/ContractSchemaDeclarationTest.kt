package fr.geoffreyCoulaud.pinryReborn.api.application

import com.fasterxml.jackson.databind.JsonNode
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoAnnotationDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadStatus
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinImageStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The contract declares what the server emits: a cursor is the opaque Base64 string the wire
 * carries, a status filled from an enum's name declares that enum's values, and a route that
 * builds its own status code declares that code.
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

    @Test
    fun `Given the published contract, Then every route declares the success codes it builds`() {
        // Given
        val built = successCodesBuiltPerRoute()

        // When
        val wrong = built.filterKeys { declaredSuccessCodes(it) != built.getValue(it) }

        // Then
        assertEquals(
            emptyMap<String, Set<String>>(),
            wrong.mapValues { (route, codes) -> "builds $codes, declares ${declaredSuccessCodes(route)}" },
            "SmallRye reads the status off the return type, so a route that builds another one " +
                "declares it with @APIResponse as BoardController does. Regenerate after " +
                "annotating: $regenerate",
        )
    }

    @Test
    fun `Given production sources, Then every route building a response of its own is read here`() {
        // Given
        val routes = restResponseRoutes()

        // When
        val unread = routes.filter { statusesIn(it).isEmpty() }.map { it.name }

        // Then
        assertTrue(routes.isNotEmpty(), "Konsist found no controller endpoint at all, so nothing here is read.")
        assertEquals(
            emptyList<String>(),
            unread,
            "These routes return a RestResponse and build no status this test recognises, so the " +
                "assertion above reads nothing of them. Add the builder to STATUS_BUILDERS.",
        )
    }

    /**
     * The success codes each route builds, keyed as the contract's operation. Two Kotlin functions
     * can serve one operation, SmallRye merging the `@Consumes`-differentiated pair of
     * `PUT /{pinId}/image`, so the codes are unioned per route rather than per function.
     */
    private fun successCodesBuiltPerRoute(): Map<String, Set<String>> =
        endpoints()
            .groupBy { it.name }
            .mapValues { (_, functions) -> functions.flatMap { statusesIn(it) }.toSet() }
            .filterValues { it.isNotEmpty() }

    /** The 2xx codes the contract declares for an operation, empty when it declares no such route. */
    private fun declaredSuccessCodes(route: String): Set<String> =
        PublishedContract.document
            .path("paths")
            .path(route.substringAfter(' '))
            .path(route.substringBefore(' ').lowercase())
            .path("responses")
            .properties()
            .map { (code, _) -> code }
            .filter { it.startsWith("2") }
            .toSet()

    /**
     * The statuses a route builds, read from its own text and from the private functions it calls
     * by name: `downloadExport` and `getImage` both hand the building to one.
     */
    private fun statusesIn(endpoint: Endpoint): Set<String> {
        val delegated = endpoint.helpers.filter { endpoint.function.text.contains("${it.name}(") }
        val text = endpoint.function.text + delegated.joinToString("\n") { it.text }
        return STATUS_BUILDERS.filterKeys { text.contains(it) }.values.toSet()
    }

    private fun restResponseRoutes(): List<Endpoint> =
        endpoints().filter { it.function.returnType?.name?.startsWith("RestResponse") == true }

    /** Every endpoint of every controller, named as the contract names its operation. */
    private fun endpoints(): List<Endpoint> =
        Konsist
            .scopeFromProduction(moduleName = "api-presentation-quarkus")
            .classes()
            .withAnnotationNamed(PATH)
            .flatMap { controller ->
                val base = controller.annotations.first { it.name == PATH }.pathValue()
                val helpers = controller.functions().filter { it.hasPrivateModifier }
                controller.functions().mapNotNull { function ->
                    val method = function.annotations.firstOrNull { it.name in HTTP_METHODS }
                    val suffix = function.annotations.firstOrNull { it.name == PATH }?.pathValue() ?: ""
                    method?.let { Endpoint("${it.name} $base$suffix", function, helpers) }
                }
            }

    private fun KoAnnotationDeclaration.pathValue(): String =
        arguments.first().value?.trim('"').orEmpty()

    private class Endpoint(
        val name: String,
        val function: KoFunctionDeclaration,
        val helpers: List<KoFunctionDeclaration>,
    )

    private companion object {
        const val PATH = "Path"
        val HTTP_METHODS = setOf("GET", "POST", "PUT", "DELETE", "PATCH")

        /**
         * What a route writes to name a status, and the status it names. `notModified` is left out:
         * `304` is not a success, and SmallRye declares none of the routes that build it.
         */
        val STATUS_BUILDERS = mapOf(
            ".created<" to "201",
            "Status.CREATED" to "201",
            "Status.ACCEPTED" to "202",
            "Status.NO_CONTENT" to "204",
            "noContent()" to "204",
            "Status.PARTIAL_CONTENT" to "206",
            "Status.OK" to "200",
            ".ok(" to "200",
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
