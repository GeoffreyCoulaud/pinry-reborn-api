package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

open class UserAuthenticationError(
    message: String,
    code: ErrorCode,
) : BaseError(message, code)

class UserAuthenticationUserDoesNotExistError :
    UserAuthenticationError(
        "User does not exist",
        ErrorCode.USER_DOES_NOT_EXIST,
    )

class UserAuthenticationInvalidPasswordError :
    UserAuthenticationError(
        "Invalid password",
        ErrorCode.INVALID_PASSWORD,
    )
