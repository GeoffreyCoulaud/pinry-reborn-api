package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

/**
 * Every code the presentation layer mints itself: refusals decided by the framework or the HTTP layer,
 * before or beside a use case. A domain refusal carries an `ErrorCode`, mapped by [BaseErrorMapper].
 */
enum class FrameworkErrorCode {
    VALIDATION_ERROR,
    AUTHENTICATION_REQUIRED,
    AUTHENTICATION_FAILED,
    SESSION_EXPIRED,
    RANGE_NOT_SATISFIABLE,
}
