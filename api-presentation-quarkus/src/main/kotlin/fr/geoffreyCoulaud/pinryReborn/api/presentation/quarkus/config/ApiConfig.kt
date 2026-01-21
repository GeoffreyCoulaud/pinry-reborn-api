package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(
    prefix = "api",
    namingStrategy = ConfigMapping.NamingStrategy.SNAKE_CASE,
)
interface ApiConfig {
    fun remoteHost(): String

    fun port(): Int

    fun basePath(): String = ""

    fun baseUrl(): String = "https://${remoteHost()}:${port()}/${basePath()}"
}
