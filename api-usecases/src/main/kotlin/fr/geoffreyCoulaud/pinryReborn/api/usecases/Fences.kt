package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TransactionRunner

/**
 * The one write of a row two actors can reach: read and written in one transaction, since a save of a
 * copy read earlier restores every column that copy carried. A refused [held] answers null (`docs/adr/0016`).
 */
internal fun <T : Any> TransactionRunner.fenced(
    read: () -> T?,
    held: (T) -> Boolean,
    update: (T) -> T,
    write: (T) -> T,
): T? = inTransaction { read()?.takeIf(held)?.let { write(update(it)) } }

/**
 * The same write, answering the row it replaced rather than the one it wrote: a caller whose release
 * depends on the state reads it here, the state it saw before the fence being possibly one old.
 */
internal fun <T : Any> TransactionRunner.fencedOver(
    read: () -> T?,
    held: (T) -> Boolean,
    update: (T) -> T,
    write: (T) -> T,
): T? = inTransaction { read()?.takeIf(held)?.also { write(update(it)) } }
