package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Image
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.ImageDownload
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.DownloadStatus
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.ImageDownloadRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.ImageRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TransactionRunner
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.ImagePermissionError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.ImagePinDoesNotExistError
import fr.geoffreyCoulaud.pinryReborn.api.utilities.TestTime
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.UUID.randomUUID

class ResolvePinImageStateTest {
    private val pins: PinRepositoryInterface = mockk()
    private val images: ImageRepositoryInterface = mockk()
    private val downloads: ImageDownloadRepositoryInterface = mockk()
    private val connection = SingleConnectionRunner()
    private val owner = User(randomUUID(), "o", createdAt = TestTime.now)
    private val pinId = randomUUID()
    private val subject = ResolvePinImageState(pins, images, downloads, connection)

    @Test fun `Given a missing pin, Then it throws ImagePinDoesNotExistError`() {
        every { pins.findPinById(pinId) } returns null
        assertThrows(ImagePinDoesNotExistError::class.java) { subject.resolve(pinId, owner) }
    }

    @Test fun `Given a non-owner, Then it throws ImagePermissionError`() {
        val otherUser = User(randomUUID(), "x", createdAt = TestTime.now)
        every { pins.findPinById(pinId) } returns Pin(pinId, otherUser, "c", null, "d", emptyList(), emptyList(),
            createdAt = TestTime.now, updatedAt = TestTime.now)
        assertThrows(ImagePermissionError::class.java) { subject.resolve(pinId, owner) }
    }

    @Test fun `Given an owner with no image and no download, Then NONE`() {
        every { pins.findPinById(pinId) } returns Pin(pinId, owner, "c", null, "d", emptyList(), emptyList(),
            createdAt = TestTime.now, updatedAt = TestTime.now)
        every { images.findByPinId(pinId) } returns null
        every { downloads.findByPinId(pinId) } returns null
        assertEquals(PinImageStatus.NONE, subject.resolve(pinId, owner).status)
    }

    @Test fun `Given a swap landing between the two reads, Then the state is one snapshot, not a torn pair`() {
        // Given: a READY jpeg with a PENDING replacement, and a writer that commits the swap the
        // moment the image was read: the new image saved and the replacement removed, together
        every { pins.findPinById(pinId) } returns Pin(pinId, owner, "c", null, "d", emptyList(), emptyList(),
            createdAt = TestTime.now, updatedAt = TestTime.now)
        val oldImage = image(pinId, "image/jpeg")
        val newImage = image(pinId, "image/png")
        val pending = ImageDownload(pinId, "https://example.com/new.png", DownloadStatus.PENDING, null, null,
            randomUUID(), TestTime.now, TestTime.now)
        var swapped = false
        every { images.findByPinId(pinId) } answers {
            val seen = if (swapped) newImage else oldImage
            connection.write { swapped = true }
            seen
        }
        every { downloads.findByPinId(pinId) } answers { if (swapped) null else pending }

        // When
        val state = subject.resolve(pinId, owner)

        // Then: the old image with its replacement, or the new one without; never the old one alone
        val consistent =
            (state.image == oldImage && state.replacement?.status == DownloadStatus.PENDING) ||
                (state.image == newImage && state.replacement == null)
        assertTrue(consistent) { "torn read: image ${state.image?.mimeType}, replacement ${state.replacement}" }
    }

    private fun image(pinId: UUID, mimeType: String) =
        Image(randomUUID(), pinId, mimeType, 1, 1, false, 1L, "h-$mimeType", "k-$mimeType", TestTime.now)

    /** The single connection's pool: a write queued while a transaction holds it lands when that ends. */
    private class SingleConnectionRunner : TransactionRunner {
        private var held = false
        private val queued = mutableListOf<() -> Unit>()

        fun write(commit: () -> Unit) {
            if (held) queued += commit else commit()
        }

        override fun <T> inTransaction(block: () -> T): T {
            held = true
            try {
                return block()
            } finally {
                held = false
                queued.forEach { it() }
                queued.clear()
            }
        }
    }
}
