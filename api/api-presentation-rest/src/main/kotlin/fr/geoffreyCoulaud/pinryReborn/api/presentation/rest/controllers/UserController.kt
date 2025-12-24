package fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.controllers

import fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.dtos.input.UserInputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.dtos.output.UserOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.mappers.UserDtoMapper.toDomain
import fr.geoffreyCoulaud.pinryReborn.api.presentation.rest.mappers.UserDtoMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.usecases.CreateUserUseCase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/users")
@ApplicationScoped
class UserController
    @Inject
    constructor(
        private val createUserUseCase: CreateUserUseCase,
    ) {
        @POST
        @Produces(MediaType.APPLICATION_JSON)
        fun createUser(userDto: UserInputDto): UserOutputDto =
            userDto
                .toDomain()
                .let { createUserUseCase.execute(it) }
                .toDto()
    }
