package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output

data class TagSearchResultOutputDto(
    val tag: TagOutputDto,
    val score: Double,
)

data class TagSearchOutputDto(
    val results: List<TagSearchResultOutputDto>,
)
