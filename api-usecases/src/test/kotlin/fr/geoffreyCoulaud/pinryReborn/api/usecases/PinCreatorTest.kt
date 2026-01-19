package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class PinCreatorTest {
    private val pinRepository: PinRepositoryInterface = mockk()
    private val tagCreator: TagCreator = mockk()
    private val useCase =
        PinCreator(
            tagCreator = tagCreator,
            pinRepository = pinRepository,
        )

    @Test
    fun `When creating a pin, then should succeed`() {
        // Given
        val user = User(randomUUID(), "John Doe")
        val sourceUrl = "https://example.com/article"
        val mediaUrl = "https://example.com/image.jpeg"
        val description = "some description"
        val tags = listOf("blue", "landscape", "water")
        every { tagCreator.findOrCreate(any(), any()) } answers {
            Tag(
                id = randomUUID(),
                name = firstArg(),
                author = secondArg()
            )
        }
        every { pinRepository.savePin(any()) } answers { firstArg() }

        // When
        val pin =
            useCase.createPin(
                author = user,
                sourceContextUrl = sourceUrl,
                sourceMediaUrl = mediaUrl,
                description = description,
                tags = tags,
            )

        // Then
        assertEquals(user, pin.author)
        assertEquals(sourceUrl, pin.sourceContextUrl)
        assertEquals(mediaUrl, pin.sourceMediaUrl)
        assertEquals(description, pin.description)
        assertEquals(tags.toSet(), pin.tags.map { it.name }.toSet())
    }
}
