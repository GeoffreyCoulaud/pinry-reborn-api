package fr.geoffreyCoulaud.pinryReborn.api.application

import io.ebean.DB
import io.ebean.Database
import org.junit.jupiter.api.BeforeEach

abstract class IntegrationTest {
    private val database: Database get() = DB.getDefault()

    /**
     * Truncate all non-internal tables in the database.
     *
     * - Tables prefixed by "sqlite_" are ignored.
     * - The "db_migration" table is ignored, as it's necessary for ebean.
     */
    @BeforeEach
    fun truncateAllTables() {
        database
            .sqlQuery("SELECT name FROM sqlite_master WHERE type='table'")
            .findList()
            .map { it.getString("name") }
            .filterNot { it.startsWith("sqlite_") or it.equals("db_migration") }
            .forEach { database.truncate(it) }
    }
}
