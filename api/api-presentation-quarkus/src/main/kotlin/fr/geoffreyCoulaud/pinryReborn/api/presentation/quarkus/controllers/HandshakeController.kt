package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.controllers

import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.config.ImagesConfig
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.config.RenditionsConfig
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.HandshakeOutputDto
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.eclipse.microprofile.config.inject.ConfigProperty

@Path("/api/v1/handshake")
class HandshakeController(
    private val imagesConfig: ImagesConfig,
    private val renditionsConfig: RenditionsConfig,
    /** The key SmallRye stamps on the contract, so the route and the document cannot drift. */
    @ConfigProperty(name = CONTRACT_VERSION_KEY) private val contractVersion: String,
) {
    @GET
    @PermitAll
    fun getHandshake(): HandshakeOutputDto = HandshakeOutputDto(
        contractVersion = contractVersion,
        limits = HandshakeOutputDto.LimitsDto(
            maxFileBytes = imagesConfig.maxFileBytes(),
            maxPixels = imagesConfig.maxPixels(),
        ),
        renditionSizes = HandshakeOutputDto.RenditionSizesDto(
            tiny = renditionsConfig.tiny(),
            small = renditionsConfig.small(),
            medium = renditionsConfig.medium(),
            large = renditionsConfig.large(),
        ),
    )

    companion object {
        const val CONTRACT_VERSION_KEY = "quarkus.smallrye-openapi.info-version"
    }
}
