package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Board
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.BoardRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TransactionRunner
import java.util.UUID

/** `saveBoard` writes every column from the copy, `softDeletedAt` included, so the read it merges is taken here. */
internal fun BoardRepositoryInterface.saveFenced(
    transactionRunner: TransactionRunner,
    boardId: UUID,
    held: (Board) -> Boolean,
    update: (Board) -> Board,
): Board? = transactionRunner.fenced({ findBoardById(boardId) }, held, update) { saveBoard(it) }
