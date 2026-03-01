package fr.geoffreyCoulaud.pinryReborn.api.usecases

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.Pin
import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.domain.repositories.PinRepositoryInterface
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinDeletionPermissionError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinDeletionPinAlreadySoftDeletedError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinDeletionPinDoesNotExistError
import fr.geoffreyCoulaud.pinryReborn.api.usecases.exceptions.PinDeletionPinNotSoftDeletedError
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID.randomUUID

class PinRecycleBinTest {
    private val pinRepository = mockk<PinRepositoryInterface>()
    private val useCase = PinRecycleBin(pinRepository = pinRepository)

    private fun createPin(author: User, softDeletedAt: Instant? = null) = Pin(
        id = randomUUID(),
        author = author,
        sourceContextUrl = "https://example.com",
        sourceMediaUrl = "https://example.com/img.jpg",
        description = "A pin",
        tags = emptyList(),
        softDeletedAt = softDeletedAt,
    )

    // --- Soft delete ---

    @Test
    fun `Given valid pin owned by user, Then soft delete succeeds`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        val pin = createPin(author = user)
        every { pinRepository.findPinById(pin.id) } returns pin
        every { pinRepository.softDeletePin(pin) } returns pin.copy(softDeletedAt = Instant.now())

        // When
        useCase.softDelete(pinId = pin.id, user = user)

        // Then
        verify { pinRepository.softDeletePin(pin) }
    }

    @Test
    fun `Given already soft-deleted pin, Then soft delete throws PinDeletionPinAlreadySoftDeletedError`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        val pin = createPin(author = user, softDeletedAt = Instant.now())
        every { pinRepository.findPinById(pin.id) } returns pin

        // When, Then
        assertThrows<PinDeletionPinAlreadySoftDeletedError> {
            useCase.softDelete(pinId = pin.id, user = user)
        }
    }

    @Test
    fun `Given pin not owned by user, Then soft delete throws PinDeletionPermissionError`() {
        // Given
        val owner = User(id = randomUUID(), name = "Owner")
        val otherUser = User(id = randomUUID(), name = "Other")
        val pin = createPin(author = owner)
        every { pinRepository.findPinById(pin.id) } returns pin

        // When, Then
        assertThrows<PinDeletionPermissionError> {
            useCase.softDelete(pinId = pin.id, user = otherUser)
        }
    }

    @Test
    fun `Given pin does not exist, Then soft delete throws PinDeletionPinDoesNotExistError`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        val pinId = randomUUID()
        every { pinRepository.findPinById(pinId) } returns null

        // When, Then
        assertThrows<PinDeletionPinDoesNotExistError> {
            useCase.softDelete(pinId = pinId, user = user)
        }
    }

    // --- Restore ---

    @Test
    fun `Given soft-deleted pin owned by user, Then restore succeeds`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        val pin = createPin(author = user, softDeletedAt = Instant.now())
        every { pinRepository.findPinById(pin.id) } returns pin
        every { pinRepository.restorePin(pin) } returns pin.copy(softDeletedAt = null)

        // When
        val result = useCase.restore(pinId = pin.id, user = user)

        // Then
        verify { pinRepository.restorePin(pin) }
        assert(result.softDeletedAt == null)
    }

    @Test
    fun `Given active pin, Then restore throws PinDeletionPinNotSoftDeletedError`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        val pin = createPin(author = user)
        every { pinRepository.findPinById(pin.id) } returns pin

        // When, Then
        assertThrows<PinDeletionPinNotSoftDeletedError> {
            useCase.restore(pinId = pin.id, user = user)
        }
    }

    // --- Permanent delete ---

    @Test
    fun `Given soft-deleted pin owned by user, Then permanent delete succeeds`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        val pin = createPin(author = user, softDeletedAt = Instant.now())
        every { pinRepository.findPinById(pin.id) } returns pin
        justRun { pinRepository.permanentlyDeletePin(pin) }

        // When
        useCase.permanentlyDelete(pinId = pin.id, user = user)

        // Then
        verify { pinRepository.permanentlyDeletePin(pin) }
    }

    @Test
    fun `Given active pin, Then permanent delete throws PinDeletionPinNotSoftDeletedError`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        val pin = createPin(author = user)
        every { pinRepository.findPinById(pin.id) } returns pin

        // When, Then
        assertThrows<PinDeletionPinNotSoftDeletedError> {
            useCase.permanentlyDelete(pinId = pin.id, user = user)
        }
    }

    // --- Empty recycle bin ---

    @Test
    fun `Given user with soft-deleted pins, Then empty recycle bin succeeds`() {
        // Given
        val user = User(id = randomUUID(), name = "John Doe")
        justRun { pinRepository.permanentlyDeleteAllSoftDeletedPinsForUser(user) }

        // When
        useCase.emptyRecycleBin(user = user)

        // Then
        verify { pinRepository.permanentlyDeleteAllSoftDeletedPinsForUser(user) }
    }
}
