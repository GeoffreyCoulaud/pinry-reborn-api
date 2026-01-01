package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

class UsernameAlreadyTakenError :
    BaseError(
        message = "Username already taken",
        code = ErrorCode.USERNAME_ALREADY_EXISTS,
    )
