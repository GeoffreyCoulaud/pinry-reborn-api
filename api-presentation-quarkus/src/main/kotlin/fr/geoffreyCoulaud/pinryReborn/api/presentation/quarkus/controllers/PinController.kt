package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.controllers

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PaginationDirection
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
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
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.PinSortStrategyMapper.toDto
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
        @QueryParam("cursor") cursor: UUID? = null,
        @QueryParam("pageSize") pageSizeInput: Int? = null,
        @QueryParam("direction") directionInput: PaginationDirectionInputEnum? = null,
        @QueryParam("sort") sortInput: PinSortStrategyInputEnum? = null,
    ): RestResponse<PinListOutputDto> {
        val user = securityIdentity.getUser()

        val pageSize = pageSizeInput ?: DEFAULT_PAGE_SIZE
        val direction = directionInput?.toDomain() ?: PaginationDirection.FORWARD
        val sort = sortInput?.toDomain() ?: PinSortStrategy.CREATED_AT_ASC

        val page = pinLister.listPinsForUserPaginated(
            user = user,
            cursor = cursor,
            direction = direction,
            pageSize = pageSize,
            sort = sort,
        )

        return RestResponse.ok(
            PinListOutputDto(
                pins = page.items.map { it.toDto() },
                pagination = PaginationDto(
                    nextPageUrl = page.nextCursor?.let {
                        buildPaginationUrl(
                            cursor = it,
                            direction = PaginationDirectionInputEnum.FORWARD,
                            pageSize = pageSize,
                            sort = sort.toDto()
                        )
                    },
                    previousPageUrl = page.previousCursor?.let {
                        buildPaginationUrl(
                            cursor = it,
                            direction = PaginationDirectionInputEnum.BACKWARD,
                            pageSize = pageSize,
                            sort = sort.toDto()
                        )
                    },
                ),
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
            .queryParam("pageSize", pageSize)
            .queryParam("sort", sort)
            .queryParam("cursor", cursor)
            .queryParam("direction", direction)
        return builder.build().toString()
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
