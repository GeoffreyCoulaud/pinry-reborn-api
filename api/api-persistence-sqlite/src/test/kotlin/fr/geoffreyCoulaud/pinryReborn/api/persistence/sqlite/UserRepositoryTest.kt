package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite

import fr.geoffreyCoulaud.pinryReborn.api.domain.entities.User
import fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class UserRepositoryTest : RepositoryTest() {
    private val repository = UserRepository(database)

    @Test
    fun `saveUser should persist user and return it with same id`() {
        // Given
        val user = User(name = "Test User")

        // When
        val savedUser = repository.saveUser(user)

        // Then
        assertEquals(user.id, savedUser.id)
        assertEquals(user.name, savedUser.name)
    }

    @Test
    fun `findUser should return user when exists`() {
        // Given
        val user = User(name = "Findable User")
        repository.saveUser(user)

        // When
        val foundUser = repository.findUser(user.id)

        // Then
        assertNotNull(foundUser)
        assertEquals(user.id, foundUser!!.id)
        assertEquals(user.name, foundUser.name)
    }

    @Test
    fun `findUser should return null when user does not exist`() {
        // When
        val foundUser = repository.findUser(randomUUID())

        // Then
        assertNull(foundUser)
    }

    @Test
    fun `deleteUser should remove user from database`() {
        // Given
        val user = User(name = "User to Delete")
        repository.saveUser(user)

        // When
        repository.deleteUser(user)

        // Then
        val foundUser = repository.findUser(user.id)
        assertNull(foundUser)
    }

    @Test
    fun `saveUser should update existing user`() {
        // Given
        val originalUser = User(name = "Original Name")
        repository.saveUser(originalUser)

        // When
        val updatedUser = originalUser.copy(name = "Updated Name")
        repository.saveUser(updatedUser)

        // Then
        val foundUser = repository.findUser(originalUser.id)
        assertNotNull(foundUser)
        assertEquals("Updated Name", foundUser!!.name)
    }
}
