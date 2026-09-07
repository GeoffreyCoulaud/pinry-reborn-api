package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import java.io.IOException

/** A `500` logged at DEBUG, as Quarkus logs an unmapped one: the client most likely terminated the connection. */
@Provider
class IOExceptionMapper : ExceptionMapper<IOException> {
    private val logger = KotlinLogging.logger {}

    @Context
    lateinit var uriInfo: UriInfo

    override fun toResponse(exception: IOException): Response {
        logger.debug(exception) { "I/O failed on ${uriInfo.path}" }
        return ProblemResponses.internalError(uriInfo).build()
    }
}
