package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.controllers

import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.config.ApiConfig
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.PaginationDirectionInputEnum
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.PinCreationInputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.PinSortStrategyInputEnum
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PaginationDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinListOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.PaginationDirectionMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.PinMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.PinSortStrategyMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.security.getUser
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinCreator
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinGetter
import fr.geoffreyCoulaud.pinryReborn.api.usecases.PinLister
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinRetrievalPermissionError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinRetrievalPinDoesNotExistError
import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.UriBuilder
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder
import java.net.URI
import java.util.UUID

@Path("/api/v1/pins")
class PinController(
    private val pinCreator: PinCreator,
    private val pinGetter: PinGetter,
    private val pinLister: PinLister,
    private val securityIdentity: SecurityIdentity,
    private val apiConfig: ApiConfig,
) {
    @GET
    @Authenticated
    fun listPins(
        @QueryParam("cursor") cursor: UUID?,
        @QueryParam("direction") direction: PaginationDirectionInputEnum,
        @QueryParam("pageSize") pageSize: Int?,
        @QueryParam("sort") sortInput: PinSortStrategyInputEnum,
    ): RestResponse<PinListOutputDto> {
        val user = securityIdentity.getUser()
        val effectivePageSize = pageSize ?: PinLister.DEFAULT_PAGE_SIZE
        val sort = sortInput.toDomain()

        val page = pinLister.listPinsForUserPaginated(
            user = user,
            cursor = cursor,
            direction = direction.toDomain(),
            pageSize = effectivePageSize,
            sort = sort,
        )

        val nextCursor = page.nextCursor
        val previousCursor = page.previousCursor

        val pagination = PaginationDto(
            nextPageUrl = nextCursor?.let {
                buildPaginationUrl(
                    it,
                    PaginationDirectionInputEnum.FORWARD,
                    effectivePageSize,
                    sortInput
                )
            },
            previousPageUrl = previousCursor?.let {
                buildPaginationUrl(
                    it,
                    PaginationDirectionInputEnum.BACKWARD,
                    effectivePageSize,
                    sortInput
                )
            },
        )

        return RestResponse.ok(
            PinListOutputDto(
                pins = page.items.map { it.toDto() },
                pagination = pagination,
            )
        )
    }

    private fun buildPaginationUrl(
        cursor: UUID,
        direction: PaginationDirectionInputEnum,
        pageSize: Int,
        sort: PinSortStrategyInputEnum
    ): String {
        val builder = UriBuilder.fromUri(apiConfig.baseUrl())
            .path("/api/v1/pins")
            .queryParam("cursor", cursor)
            .queryParam("direction", direction.name)
            .queryParam("pageSize", pageSize)
        if (sort != null) {
            builder.queryParam("sort", sort.name)
        }
        return builder.build().toString()
    }

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
}
