package fr.geoffreyCoulaud.pinryReborn.api.application

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * The deployment's own `api.cors.origins`, taken from the production file. Every other case runs
 * with the test resources' `https://app.test`, so this profile is the only place the shipped default
 * is exercised.
 */
class ProductionCorsTestProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> =
        mapOf(ORIGINS_KEY to (ProductionProperties[ORIGINS_KEY] ?: error("$ORIGINS_KEY is not declared")))

    companion object {
        const val ORIGINS_KEY = "api.cors.origins"
    }
}
