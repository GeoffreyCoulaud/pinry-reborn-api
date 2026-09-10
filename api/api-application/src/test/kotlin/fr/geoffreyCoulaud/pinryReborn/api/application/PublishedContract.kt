package fr.geoffreyCoulaud.pinryReborn.api.application

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

/**
 * `contract/openapi.json`, read from the working tree: inside `dagger call gate` the same build
 * wrote it moments earlier, and the test resources blank `store-schema-directory` so a test run
 * never rewrites it under the assertion.
 */
object PublishedContract {
    val document: JsonNode = ObjectMapper().readTree(File("../../contract/openapi.json"))

    /** The component schema under `components.schemas`, missing when the contract declares none. */
    fun schema(name: String): JsonNode = document.path("components").path("schemas").path(name)
}
