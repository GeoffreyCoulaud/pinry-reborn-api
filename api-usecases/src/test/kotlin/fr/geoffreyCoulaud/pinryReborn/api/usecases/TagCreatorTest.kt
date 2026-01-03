package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
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
        val user = User(id = randomUUID(), name = "John Doe")
        val tagString = createRandomString()
        every { tagRepository.findUserTagByName(user, tagString) } returns null
        every { tagRepository.saveTag(any()) } answers { firstArg() }

        // When, Then
        assertDoesNotThrow {
            useCase.findOrCreate(name = tagString, user = user)
        }
    }

    @Test
    fun `When trying to re-create an existing tag, should return the existing tag`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        val tagString = createRandomString()
        val tag = Tag(id = randomUUID(), name = tagString, author = user)
        every { tagRepository.findUserTagByName(user = user, name = tagString) } returns tag

        // When
        val result = useCase.findOrCreate(user = user, name = tagString)

        // Then
        assertEquals(result, tag)
    }
}
