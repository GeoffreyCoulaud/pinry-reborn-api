package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Image
import fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.mappers.ImageMapper.toDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID.randomUUID

class ImageMapperTest {
    @Test
    fun `Given an image, Then toDto builds a serve url relative to the origin serving it`() {
        val pinId = randomUUID()
        val image =
            Image(randomUUID(), pinId, "image/webp", 8, 6, false, 99, "h", "originals/x/y/z.webp", Instant.EPOCH)
        val dto = image.toDto()
        assertEquals("/api/v1/pins/$pinId/image", dto.url)
        assertEquals("image/webp", dto.mimeType)
        assertEquals(8, dto.width)
    }
}
