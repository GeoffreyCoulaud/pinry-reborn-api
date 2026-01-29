package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

data class PinListOutputDto(
    val pins: List<PinOutputDto>,
    val pagination: PaginationOutputDto,
)
