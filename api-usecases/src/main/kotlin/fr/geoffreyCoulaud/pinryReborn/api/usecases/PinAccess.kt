package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TransactionRunner
import java.util.UUID

/** `savePin` writes every column and both link tables from the copy, so the read it merges is taken here. */
internal fun PinRepositoryInterface.saveFenced(
    transactionRunner: TransactionRunner,
    pinId: UUID,
    held: (Pin) -> Boolean,
    update: (Pin) -> Pin,
): Pin? = transactionRunner.fenced({ findPinById(pinId) }, held, update) { savePin(it) }
