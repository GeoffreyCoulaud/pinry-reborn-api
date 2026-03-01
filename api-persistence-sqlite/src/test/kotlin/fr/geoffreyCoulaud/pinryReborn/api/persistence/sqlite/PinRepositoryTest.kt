package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Tag
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.enums.PinSortStrategy
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.models.PinModel
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories.PinRepository
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories.TagRepository
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories.UserRepository
import fr.geoffreyCoulaud.pinryReborn.api.utilities.createRandomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/image.jpeg",
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
        assertEquals(pin.sourceContextUrl, model.sourceContextUrl)
        assertEquals(pin.sourceMediaUrl, model.sourceMediaUrl)
        assertEquals(pin.description, model.description)
    }

    @Test
    fun `When saving an existing pin, then should update it`() {
        // Given
        val pin = createPin()
        repository.savePin(pin)
        val updatedPin =
            pin.copy(
                sourceContextUrl = "https://new-example.com/new.jpeg",
                sourceMediaUrl = "https://new-example.com/new_image.jpeg",
                description = "New description",
            )

        // When
        repository.savePin(updatedPin)

        // Then
        val model = database.find(PinModel::class.java, pin.id)
        assertNotNull(model)
        assertEquals(pin.id, model!!.id)
        assertEquals(updatedPin.sourceContextUrl, model.sourceContextUrl)
        assertEquals(updatedPin.sourceMediaUrl, model.sourceMediaUrl)
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

    // --- Soft delete tests ---

    private fun createAndSavePin(author: User): Pin {
        val pin = Pin(
            id = randomUUID(),
            author = author,
            sourceContextUrl = "https://example.com",
            sourceMediaUrl = "https://example.com/image.jpeg",
            description = "Something",
            tags = emptyList(),
        )
        return repository.savePin(pin)
    }

    @Test
    fun `Given soft-deleted pin, Then findPinsForUser excludes it`() {
        // Given
        val user = createAndSaveUser()
        val pin = createAndSavePin(user)
        repository.softDeletePin(pin)

        // When
        val page = repository.findPinsForUser(
            reader = user,
            cursor = null,
            pageSize = 10,
            sortStrategy = PinSortStrategy.CREATED_AT_ASC,
        )

        // Then
        assertTrue(page.items.isEmpty())
    }

    @Test
    fun `Given soft-deleted pin, Then findAllPinsForUser excludes it`() {
        // Given
        val user = createAndSaveUser()
        val pin = createAndSavePin(user)
        repository.softDeletePin(pin)

        // When
        val pins = repository.findAllPinsForUser(user)

        // Then
        assertTrue(pins.isEmpty())
    }

    @Test
    fun `Given soft-deleted pin, Then findSoftDeletedPinsForUser includes it`() {
        // Given
        val user = createAndSaveUser()
        val pin = createAndSavePin(user)
        repository.softDeletePin(pin)

        // When
        val page = repository.findSoftDeletedPinsForUser(
            reader = user,
            cursor = null,
            pageSize = 10,
            sortStrategy = PinSortStrategy.CREATED_AT_ASC,
        )

        // Then
        assertEquals(1, page.items.size)
        assertEquals(pin.id, page.items[0].id)
    }

    @Test
    fun `Given pin, Then softDeletePin sets softDeletedAt`() {
        // Given
        val user = createAndSaveUser()
        val pin = createAndSavePin(user)

        // When
        val result = repository.softDeletePin(pin)

        // Then
        assertNotNull(result.softDeletedAt)
    }

    @Test
    fun `Given soft-deleted pin, Then restorePin clears softDeletedAt`() {
        // Given
        val user = createAndSaveUser()
        val pin = createAndSavePin(user)
        val softDeleted = repository.softDeletePin(pin)

        // When
        val result = repository.restorePin(softDeleted)

        // Then
        assertNull(result.softDeletedAt)
    }

    @Test
    fun `Given soft-deleted pin, Then permanentlyDeletePin removes it and its tag associations`() {
        // Given
        val user = createAndSaveUser()
        val tag = createAndSaveTag(name = "tag1", user = user)
        val pin = createPinWithTags(tag).copy(author = user)
        repository.savePin(pin)
        val softDeleted = repository.softDeletePin(pin)

        // When
        repository.permanentlyDeletePin(softDeleted)

        // Then
        assertNull(repository.findPinById(pin.id))
    }

    @Test
    fun `Given multiple soft-deleted pins, Then permanentlyDeleteAllSoftDeletedPinsForUser removes all`() {
        // Given
        val user = createAndSaveUser()
        val pin1 = createAndSavePin(user)
        val pin2 = createAndSavePin(user)
        val activePin = createAndSavePin(user)
        repository.softDeletePin(pin1)
        repository.softDeletePin(pin2)

        // When
        repository.permanentlyDeleteAllSoftDeletedPinsForUser(user)

        // Then
        assertNull(repository.findPinById(pin1.id))
        assertNull(repository.findPinById(pin2.id))
        assertNotNull(repository.findPinById(activePin.id))
    }
}
