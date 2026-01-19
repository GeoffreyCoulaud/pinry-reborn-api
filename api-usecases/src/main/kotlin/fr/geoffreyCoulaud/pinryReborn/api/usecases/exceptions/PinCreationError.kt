package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

open class PinCreationError(
    message: String,
    code: ErrorCode,
    cause: Throwable? = null
) :
    BaseError(message, code, cause)

class PinCreationBadLoginError(
    cause: Throwable,
) : PinCreationError("Bad Login", ErrorCode.INVALID_LOGIN, cause = cause)
