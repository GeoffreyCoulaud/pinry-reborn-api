package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.config

import io.smallrye.config.ConfigMapping
import java.util.Optional

@ConfigMapping(
    prefix = "api",
    namingStrategy = ConfigMapping.NamingStrategy.SNAKE_CASE,
)
interface ApiConfig {
    fun host(): String

    fun remoteHost(): String

    fun port(): Int

    fun basePath(): String = ""

    fun baseUrl(): String = "https://${remoteHost()}:${port()}/${basePath()}"

    fun cors(): Cors

    interface Cors {
        /**
         * Allowed CORS origins. Forwarded verbatim to `quarkus.http.cors.origins` via
         * `api.cors.origins` interpolation; the built-in CORS filter reads the framework property, so
         * this typed member exists to keep the public `api.*` surface complete and validated, not to
         * be read by application code.
         *
         * Optional because the shipped list is empty: SmallRye's converter reads an empty value as
         * null and refuses to build a required member from it.
         */
        fun origins(): Optional<String>
    }
}
