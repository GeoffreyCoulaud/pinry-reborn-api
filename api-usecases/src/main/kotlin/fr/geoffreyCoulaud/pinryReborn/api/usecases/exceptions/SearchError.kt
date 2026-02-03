package fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions

sealed class SearchError(
    message: String,
    code: ErrorCode,
    cause: Throwable? = null,
) : BaseError(message, code, cause)

class SearchEmptyQueryError :
    SearchError(
        message = "Search query cannot be empty",
        code = ErrorCode.SEARCH_EMPTY_QUERY,
    )
