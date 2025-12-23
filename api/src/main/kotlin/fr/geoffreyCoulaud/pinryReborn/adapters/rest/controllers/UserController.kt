package fr.geoffreyCoulaud.pinryReborn.adapters.rest.controllers

import fr.geoffreyCoulaud.pinryReborn.adapters.rest.dtos.out.UserDto
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
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    fun createUserRoute(name: String): UserDto {
        val user = createUserUseCase.execute(name)
        return UserDto(id = user.id, name = user.name)
    }
}
