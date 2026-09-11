package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * [sourceMediaUrl] is nullable: a pin whose image is uploaded from disk names no media, which the
 * domain and the column have always allowed. The controller reads a blank one as none.
 */
data class PinCreationInputDto(
    @field:NotBlank
    val sourceContextUrl: String,
    val sourceMediaUrl: String?,
    @field:Size(max = 2000)
    val description: String,
)
