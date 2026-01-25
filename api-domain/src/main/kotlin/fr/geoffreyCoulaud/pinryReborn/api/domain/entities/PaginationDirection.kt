package fr.geoffreyCoulaud.pinryReborn.api.domain.entities

enum class PaginationDirection {
    /**
     * Signal to return the next page in the pagination context.
     *
     * Example:
     * - data = 1..100
     * - cursor = 50
     * - direction = FORWARD
     * - Result = 51..100
     */
    FORWARD,

    /**
     * Signal to return the previous page in the pagination context.
     *
     * Example
     * - data = 1..100
     * - cursor = 50
     * - direction = BACKWARD
     * - Result = 0..49
     */
    BACKWARD,
}
