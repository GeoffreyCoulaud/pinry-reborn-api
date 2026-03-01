package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

open class PinTaggingError(
    message: String,
    code: ErrorCode,
    cause: Throwable? = null
) :
    BaseError(message, code, cause)

class PinTaggingPinDoesNotExistError : PinTaggingError(
    "Pin does not exist",
    ErrorCode.PIN_DOES_NOT_EXIST
)

class PinTaggingPermissionError : PinTaggingError(
    "Insufficient permissions",
    ErrorCode.PIN_INSUFFICIENT_PERMISSIONS
)

class PinTaggingSoftDeletedPinError : PinTaggingError(
    "Cannot tag a soft-deleted pin",
    ErrorCode.PIN_ALREADY_SOFT_DELETED
)
