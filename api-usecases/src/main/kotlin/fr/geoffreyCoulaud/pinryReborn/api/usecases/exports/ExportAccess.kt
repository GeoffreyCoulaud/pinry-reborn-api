package fr.geoffreyCoulaud.pinryReborn.api.usecases.exports

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.UserDataExport
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TransactionRunner
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.UserDataExportRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.fenced
import fr.geoffreyCoulaud.pinryReborn.api.usecases.fencedOver
import java.util.UUID

/** The export row's fence; an absent row refuses, `merge` being an upsert. The write is a lambda for the rule. */
internal fun UserDataExportRepositoryInterface.saveFenced(
    transactionRunner: TransactionRunner,
    exportId: UUID,
    held: (UserDataExport) -> Boolean,
    update: (UserDataExport) -> UserDataExport,
): UserDataExport? = transactionRunner.fenced({ findById(exportId) }, held, update) { save(it) }

/** The same fence answering the row it replaced: a caller whose release depends on the state reads it here. */
internal fun UserDataExportRepositoryInterface.saveFencedOver(
    transactionRunner: TransactionRunner,
    exportId: UUID,
    held: (UserDataExport) -> Boolean,
    update: (UserDataExport) -> UserDataExport,
): UserDataExport? = transactionRunner.fencedOver({ findById(exportId) }, held, update) { save(it) }
