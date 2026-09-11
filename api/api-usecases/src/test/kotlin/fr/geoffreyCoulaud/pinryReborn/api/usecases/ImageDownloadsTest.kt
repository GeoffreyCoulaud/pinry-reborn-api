package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.ImageDownload
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadReason
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadStatus
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.ImageDownloadRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TransactionRunner
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.ImageDownloadDoesNotExistError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.ImageDownloadInProgressError
import fr.geoffreyCoulaud.pinryReborn.api.utilities.TestTime
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID

class ImageDownloadsTest {
    private val downloads: ImageDownloadRepositoryInterface = mockk(relaxed = true)
    private val runner: TransactionRunner = mockk()
    private val requester = User(randomUUID(), "o", createdAt = TestTime.now)
    private val pinId = randomUUID()

    private val subject = ImageDownloads(downloads, runner)

    init {
        every { runner.inTransaction<Unit>(any()) } answers { firstArg<() -> Unit>().invoke() }
    }

    private fun row(status: DownloadStatus, pin: UUID = pinId) = ImageDownload(
        pinId = pin,
        sourceUrl = "https://x/i.png",
        status = status,
        reasonCode = if (status == DownloadStatus.FAILED) DownloadReason.NOT_FOUND else null,
        lastError = null,
        taskId = randomUUID(),
        requestedAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )

    @Test
    fun `Given rows the requester owns, Then list hands back what the traversal found`() {
        // Given
        val rows = listOf(row(DownloadStatus.PENDING), row(DownloadStatus.FAILED, randomUUID()))
        every { downloads.findByAuthor(requester.id) } returns rows

        // When / Then
        assertEquals(rows, subject.list(requester))
    }

    @Test
    fun `Given a failed row of the requester, Then delete drops it`() {
        // Given
        every { downloads.findByAuthor(requester.id) } returns listOf(row(DownloadStatus.FAILED))

        // When
        subject.delete(requester, pinId)

        // Then
        verify { downloads.deleteByPinId(pinId) }
    }

    @Test
    fun `Given a running row, Then delete throws ImageDownloadInProgressError and keeps it`() {
        // Given
        every { downloads.findByAuthor(requester.id) } returns listOf(row(DownloadStatus.PENDING))

        // When / Then
        assertThrows(ImageDownloadInProgressError::class.java) { subject.delete(requester, pinId) }
        verify(exactly = 0) { downloads.deleteByPinId(any()) }
    }

    @Test
    fun `Given a row the requester cannot see, Then delete throws ImageDownloadDoesNotExistError`() {
        // Given: the traversal answers another pin's row, so this one is absent to the requester
        every { downloads.findByAuthor(requester.id) } returns listOf(row(DownloadStatus.FAILED, randomUUID()))

        // When / Then
        assertThrows(ImageDownloadDoesNotExistError::class.java) { subject.delete(requester, pinId) }
        verify(exactly = 0) { downloads.deleteByPinId(any()) }
    }
}
