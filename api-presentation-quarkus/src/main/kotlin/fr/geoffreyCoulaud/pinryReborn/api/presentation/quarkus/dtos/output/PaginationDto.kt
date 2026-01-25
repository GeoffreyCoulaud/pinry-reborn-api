package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

data class PaginationDto(
    val nextPageUrl: String?,
    val previousPageUrl: String?,
)