package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PasswordHashAlgorithm

data class HashedPassword(
    val hash: String,
    val algorithm: PasswordHashAlgorithm,
)
