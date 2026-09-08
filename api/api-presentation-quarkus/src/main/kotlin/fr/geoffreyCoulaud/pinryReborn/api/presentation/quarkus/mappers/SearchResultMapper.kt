package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.SearchResult
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinSearchOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.PinSearchResultOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.TagSearchOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.TagSearchResultOutputDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.PinMapper.toDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.TagMapper.toDto

object SearchResultMapper {
    fun List<SearchResult<Tag>>.toTagSearchDto() = TagSearchOutputDto(
        results = this.map { result ->
            TagSearchResultOutputDto(
                tag = result.item.toDto(),
                score = result.score,
            )
        }
    )

    fun List<SearchResult<Pin>>.toPinSearchDto() = PinSearchOutputDto(
        results = this.map { result ->
            PinSearchResultOutputDto(
                pin = result.item.toDto(),
                score = result.score,
            )
        }
    )
}
