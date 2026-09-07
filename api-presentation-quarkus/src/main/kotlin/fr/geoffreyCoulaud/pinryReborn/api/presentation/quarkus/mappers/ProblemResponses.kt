package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.ProblemDetail
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo

/** Builds every RFC 7807 payload the mappers answer. Callers may add headers before build(). */
object ProblemResponses {
    const val PROBLEM_JSON_MEDIA_TYPE = "application/problem+json"

    /** RFC 7807 challenge value: opaque bearer token, no realm. */
    const val WWW_AUTHENTICATE_BEARER = "Bearer"

    fun problemResponse(
        status: Response.Status,
        detail: String?,
        code: String,
        uriInfo: UriInfo,
    ): Response.ResponseBuilder = problemResponse(status.statusCode, status.reasonPhrase, detail, code, uriInfo)

    /** For a raw status code, since not every mapped status has a [Response.Status] constant (422, 507). */
    @Suppress("LongParameterList")
    fun problemResponse(
        status: Int,
        title: String,
        detail: String?,
        code: String,
        uriInfo: UriInfo,
        currentLength: Long? = null,
    ): Response.ResponseBuilder =
        Response
            .status(status)
            .entity(
                ProblemDetail(
                    title = title,
                    status = status,
                    detail = detail,
                    instance = uriInfo.path,
                    code = code,
                    currentLength = currentLength,
                ),
            )
            .type(PROBLEM_JSON_MEDIA_TYPE)
}
