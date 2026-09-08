package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

data class PinSearchResultOutputDto(
    val pin: PinOutputDto,
    val score: Double,
)

data class PinSearchOutputDto(
    val results: List<PinSearchResultOutputDto>,
)
