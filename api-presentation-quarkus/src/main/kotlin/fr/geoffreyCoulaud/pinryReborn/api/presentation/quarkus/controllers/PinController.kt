package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.controllers

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.config.ApiConfig
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.common.CursorDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.PinCreationInputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.PinSortStrategyInputEnum
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PaginationOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinListOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.CursorMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.CursorMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.PinMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.PinSortStrategyMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.security.getUser
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.serialization.Base64Json
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinCreator
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinGetter
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinRetrievalPermissionError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinRetrievalPinDoesNotExistError
import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder
import java.net.URI
import java.util.UUID

@Path("/api/v1/pins")
class PinController(
    private val pinCreator: PinCreator,
    private val pinGetter: PinGetter,
    private val securityIdentity: SecurityIdentity,
    private val apiConfig: ApiConfig,
) {
    @GET
    @Authenticated
    @Path("/{pinId}")
    fun getPin(pinId: UUID): RestResponse<PinOutputDto> {
        val user = securityIdentity.getUser()
        try {
            val pin = pinGetter.getPinForUser(pinId = pinId, reader = user)
            return RestResponse.ok(pin.toDto())
        } catch (_: PinRetrievalPinDoesNotExistError) {
            return RestResponse.notFound()
        } catch (_: PinRetrievalPermissionError) {
            return ResponseBuilder
                .create<PinOutputDto>(RestResponse.Status.FORBIDDEN)
                .build()
        }
    }

    @POST
    @Authenticated
    fun createPin(creationDto: PinCreationInputDto): RestResponse<PinOutputDto> {
        val author = securityIdentity.getUser()
        val pin =
            pinCreator.createPin(
                author = author,
                sourceContextUrl = creationDto.sourceContextUrl,
                sourceMediaUrl = creationDto.sourceMediaUrl,
                description = creationDto.description,
                tags = emptyList(),
            )
        return ResponseBuilder
            .created<PinOutputDto>(URI("${apiConfig.baseUrl()}/api/v1/pins/${pin.id}"))
            .entity(pin.toDto())
            .build()
    }

    @GET
    @Authenticated
    fun listPins(
        @QueryParam("cursor") @Base64Json cursorInput: CursorDto? = null,
        @QueryParam("pageSize") pageSizeInput: Int? = null,
        @QueryParam("sort") sortInput: PinSortStrategyInputEnum? = null,
    ): RestResponse<PinListOutputDto> {
        val user = securityIdentity.getUser()
        val pageSize = pageSizeInput ?: DEFAULT_PAGE_SIZE
        val sort = sortInput?.toDomain() ?: PinSortStrategy.CREATED_AT_ASC
        val cursor = cursorInput?.let { cursorInput.toDomain() }

        val page =
            pinGetter.listPinsPaginatedForUser(
                reader = user,
                cursor = cursor,
                pageSize = pageSize,
                sort = sort,
            )

        val pagination =
            PaginationOutputDto(
                previousCursor = page.previousCursor?.toDto(),
                nextCursor = page.nextCursor?.toDto(),
            )
        return RestResponse.ok(
            PinListOutputDto(
                pins = page.items.map { it.toDto() },
                pagination = pagination,
            ),
        )
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
