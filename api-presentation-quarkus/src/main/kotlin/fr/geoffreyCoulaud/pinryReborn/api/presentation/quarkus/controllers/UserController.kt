package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.controllers

import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input.UserInputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.UserOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.UserDtoMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.UserDtoMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.usecases.UserCreator
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.jboss.resteasy.reactive.RestResponse

@Path("/api/v1/users")
class UserController(
    private val userCreator: UserCreator,
) {
    @POST
    fun createUser(userDto: UserInputDto): RestResponse<UserOutputDto> {
        val userOutputDto = userDto.toDomain().let { userCreator.createUser(it) }.toDto()
        return RestResponse.ok(userOutputDto)
    }
}
