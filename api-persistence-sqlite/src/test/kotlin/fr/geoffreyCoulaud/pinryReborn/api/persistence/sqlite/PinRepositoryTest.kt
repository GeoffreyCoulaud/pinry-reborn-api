package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories.PinRepository
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories.TagRepository
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories.UserRepository
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class PinRepositoryTest : RepositoryTest() {
    private val repository = PinRepository(database)
    private val userRepository = UserRepository(database)
    private val tagRepository = TagRepository(database)

    private fun createAndSaveUser(): User =
        userRepository.saveUser(
            User(
                id = randomUUID(),
                name = createRandomString(),
            ),
        )

    private fun createAndSaveTag(
        name: String,
        user: User,
    ): Tag =
        tagRepository.saveTag(
            Tag(
                id = randomUUID(),
                author = user,
                name = name,
            ),
        )

    private fun createPin(): Pin =
        Pin(
            id = randomUUID(),
            author = createAndSaveUser(),
            sourceUrl = "https://example.com",
            mediaUrl = "https://example.com/image.jpeg",
            description = "Something",
            tags = emptyList(),
        )

    private fun createPinWithTags(vararg tags: Tag): Pin =
        createPin()
            .copy(tags = tags.toList())

    @Test
    fun `When saving a new pin, then should create it`() {
        // Given
        val pin = createPin()

        // When
        repository.savePin(pin)

        // Then
        val model = database.find(PinModel::class.java, pin.id)
        assertNotNull(model)
        assertEquals(pin.id, model!!.id)
        assertEquals(pin.author.id, model.author.id)
        assertEquals(pin.sourceUrl, model.sourceUrl)
        assertEquals(pin.mediaUrl, model.mediaUrl)
        assertEquals(pin.description, model.description)
    }

    @Test
    fun `When saving an existing pin, then should update it`() {
        // Given
        val pin = createPin()
        repository.savePin(pin)
        val updatedPin =
            pin.copy(
                sourceUrl = "https://new-example.com/new.jpeg",
                mediaUrl = "https://new-example.com/new_image.jpeg",
                description = "New description",
            )

        // When
        repository.savePin(updatedPin)

        // Then
        val model = database.find(PinModel::class.java, pin.id)
        assertNotNull(model)
        assertEquals(pin.id, model!!.id)
        assertEquals(updatedPin.sourceUrl, model.sourceUrl)
        assertEquals(updatedPin.mediaUrl, model.mediaUrl)
        assertEquals(updatedPin.description, model.description)
    }

    @Test
    fun `When getting a pin, then should return it`() {
        // Given
        val pin = createPin()
        repository.savePin(pin)

        // When
        val actual = repository.findPinById(pin.id)

        // Then
        assertNotNull(actual)
        assertEquals(pin, actual!!)
    }

    @Test
    fun `When getting a nonexistent pin, then should return null`() {
        // Given
        // When
        val actual = repository.findPinById(randomUUID())

        // Then
        assertNull(actual)
    }

    @Test
    fun `When changing a pin's tag, then should properly update them`() {
        // Given
        val user = createAndSaveUser()
        val tag1 = createAndSaveTag(name = "tag1", user = user)
        val tag2 = createAndSaveTag(name = "tag2", user = user)
        val tag3 = createAndSaveTag(name = "tag3", user = user)
        val pin = createPinWithTags(tag1, tag2)
        repository.savePin(pin)
        val updatedPin = pin.copy(tags = listOf(tag2, tag3))

        // When
        repository.savePin(updatedPin)

        // Then
        val actual = repository.findPinById(pin.id)
        assertNotNull(actual)
        assertEquals(setOf(tag2, tag3), actual!!.tags.toSet())
    }
}
