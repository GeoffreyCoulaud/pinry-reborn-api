package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.input

data class PinCreationInputDto(
    val sourceContextUrl: String,
    val sourceMediaUrl: String,
    val description: String
)
