package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

open class LoginError(
    message: String,
    code: ErrorCode,
) : BaseError(
        message,
        code,
    )

class LoginUserDoesNotExistError :
    LoginError(
        "User does not exist",
        ErrorCode.USER_DOES_NOT_EXIST,
    )

class LoginInvalidPasswordError :
    LoginError(
        "Invalid password",
        ErrorCode.INVALID_PASSWORD,
    )
