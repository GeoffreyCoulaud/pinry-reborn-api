package fr.geoffreyCoulaud.pinryReborn.api.utilities

import io.mockk.checkUnnecessaryStub
import org.junit.jupiter.api.AfterEach

abstract class BaseTest {
    @AfterEach
    fun afterEachCheckUnnecessaryStub() {
        checkUnnecessaryStub()
    }
}
