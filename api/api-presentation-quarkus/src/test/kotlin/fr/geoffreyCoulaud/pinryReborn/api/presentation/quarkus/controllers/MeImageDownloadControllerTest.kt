package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.controllers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.ImageDownload
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadStatus
import fr.geoffreyCoulaud.pinryReborn.api.usecases.ImageDownloads
import fr.geoffreyCoulaud.pinryReborn.api.utilities.TestTime
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.quarkus.security.identity.SecurityIdentity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID.randomUUID

class MeImageDownloadControllerTest {
    private val user = User(randomUUID(), "alice", createdAt = TestTime.now)
    private val identity = mockk<SecurityIdentity> { every { getAttribute<User>("user") } returns user }
    private val imageDownloads = mockk<ImageDownloads>(relaxed = true)
    private val pinId = randomUUID()

    private val controller = MeImageDownloadController(imageDownloads, identity)

    @Test
    fun `Given the caller's downloads, Then the list carries one item per row`() {
        // Given
        every { imageDownloads.list(user) } returns
            listOf(
                ImageDownload(pinId, "https://x/i.png", DownloadStatus.PENDING, null, null, randomUUID(),
                    Instant.EPOCH, Instant.EPOCH),
            )

        // When
        val dto = controller.listImageDownloads()

        // Then
        assertEquals(listOf(pinId), dto.downloads.map { it.pinId })
    }

    @Test
    fun `Given a pin id, Then the deletion is scoped to the caller and answers 204`() {
        // When
        val response = controller.deleteImageDownload(pinId)

        // Then
        assertEquals(NO_CONTENT, response.status)
        verify { imageDownloads.delete(user, pinId) }
    }

    private companion object {
        const val NO_CONTENT = 204
    }
}
