package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.TagRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID.randomUUID

class TagCreatorTest {
    private val tagRepository = mockk<TagRepositoryInterface>()
    private val useCase = TagCreator(tagRepository = tagRepository)

    @Test
    fun `When creating a new tag, should succeed`() {
        // Given
        val tagString = createRandomString()
        every { tagRepository.findTagByName(tagString) } returns null
        every { tagRepository.saveTag(any()) } answers { firstArg() }

        // When, Then
        assertDoesNotThrow {
            useCase.findOrCreate(tagString)
        }
    }

    @Test
    fun `When trying to re-create an existing tag, should return the existing tag`() {
        // Given
        val tagString = createRandomString()
        val tag = Tag(id = randomUUID(), name = tagString)
        every { tagRepository.findTagByName(tagString) } returns tag

        // When
        val result = useCase.findOrCreate(tagString)

        // Then
        assertEquals(result, tag)
    }
}
