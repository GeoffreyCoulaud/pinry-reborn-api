package fr.geoffreyCoulaud.pinryReborn.api.utilities

import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class BaseTest {
    @BeforeEach
    fun beforeEachClearMocks() {
        clearAllMocks()
        unmockkAll()
    }

    @AfterEach
    fun afterEachCheckUnnecessaryStub() {
        checkUnnecessaryStub()
        clearAllMocks()
        unmockkAll()
    }
}
