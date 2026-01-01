package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

class PinCreationBadLoginError(
    cause: Throwable,
) : BaseError("Bad Login", ErrorCode.INVALID_LOGIN, cause = cause)
