package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.exceptions

abstract class PersistenceException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
