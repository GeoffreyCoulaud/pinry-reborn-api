package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

open class BaseError(
    override val message: String?,
    val code: ErrorCode,
    override val cause: Throwable? = null,
) : Exception("[$code] $message", cause)
