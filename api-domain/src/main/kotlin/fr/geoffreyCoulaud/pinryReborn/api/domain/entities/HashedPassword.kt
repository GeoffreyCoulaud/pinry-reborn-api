package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

data class HashedPassword(
    val hash: String,
    val algorithm: PasswordHashAlgorithm,
)
