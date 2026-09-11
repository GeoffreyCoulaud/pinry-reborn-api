package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.ImageDownload
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadReason
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadStatus
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.dtos.output.DownloadStatusDto
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.ImageDownloadDtoMapper.toDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID.randomUUID

class ImageDownloadDtoMapperTest {
    private val pinId = randomUUID()

    private fun download(status: DownloadStatus, reason: DownloadReason?) = ImageDownload(
        pinId = pinId,
        sourceUrl = "https://x/i.png",
        status = status,
        reasonCode = reason,
        lastError = "a transient error nobody outside the server reads",
        taskId = randomUUID(),
        requestedAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )

    @Test
    fun `Given a failed download, Then the dto carries its reason code and a message`() {
        // Given / When
        val dto = download(DownloadStatus.FAILED, DownloadReason.ACCESS_DENIED).toDto()

        // Then
        assertEquals(DownloadStatusDto.FAILED, dto.status)
        assertEquals("ACCESS_DENIED", dto.reasonCode)
        assertTrue(dto.message!!.isNotBlank())
    }

    @Test
    fun `Given a running download, Then the dto carries the source url and no reason`() {
        // Given / When
        val dto = download(DownloadStatus.PENDING, null).toDto()

        // Then
        assertEquals(DownloadStatusDto.PENDING, dto.status)
        assertEquals(pinId, dto.pinId)
        assertEquals("https://x/i.png", dto.sourceUrl)
        assertNull(dto.reasonCode)
        assertNull(dto.message)
    }

    @Test
    fun `Given a list of downloads, Then the dto holds one item per row`() {
        // Given / When
        val dto = listOf(download(DownloadStatus.PENDING, null)).toDto()

        // Then
        assertEquals(listOf(pinId), dto.downloads.map { it.pinId })
    }
}
