package fr.geoffreyCoulaud.pinryReborn.adapters.rest.controllers

import fr.geoffreyCoulaud.pinryReborn.adapters.rest.dtos.input.UserInputDto
import fr.geoffreyCoulaud.pinryReborn.adapters.rest.dtos.output.UserOutputDto
import fr.geoffreyCoulaud.pinryReborn.adapters.rest.mappers.UserDtoMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.adapters.rest.mappers.UserDtoMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.application.CreateUserUseCase
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/users")
class UserController(
    private val createUserUseCase: CreateUserUseCase,
) {
    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    fun createUser(userDto: UserInputDto): UserOutputDto =
        userDto
            .toDomain()
            .let { createUserUseCase.execute(it) }
            .toDto()
}
