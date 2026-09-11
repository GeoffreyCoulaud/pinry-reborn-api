package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

open class ImageError(message: String, code: ErrorCode, cause: Throwable? = null) : BaseError(message, code, cause)

class ImagePinDoesNotExistError : ImageError("Pin does not exist", ErrorCode.IMAGE_DOES_NOT_EXIST)

class ImagePermissionError : ImageError("Insufficient permissions", ErrorCode.IMAGE_INSUFFICIENT_PERMISSIONS)

class ImageDoesNotExistError : ImageError("Pin has no image", ErrorCode.IMAGE_DOES_NOT_EXIST)

class ImageTooLargeError(cause: Throwable? = null) :
    ImageError("Image exceeds the maximum size", ErrorCode.IMAGE_TOO_LARGE, cause)

class ImageInvalidError(message: String, cause: Throwable? = null) : ImageError(message, ErrorCode.IMAGE_INVALID, cause)

class ImageSourceUrlInvalidError(cause: Throwable? = null) :
    ImageError("Invalid source URL", ErrorCode.IMAGE_SOURCE_URL_INVALID, cause)

// The image family's 404, as for a pin nobody can reach: the message names the case, the code names
// the family, and a requester learns nothing about another account's rows.
class ImageDownloadDoesNotExistError : ImageError("Pin has no image download", ErrorCode.IMAGE_DOES_NOT_EXIST)

class ImageDownloadInProgressError :
    ImageError("The download is still running", ErrorCode.IMAGE_DOWNLOAD_IN_PROGRESS)
