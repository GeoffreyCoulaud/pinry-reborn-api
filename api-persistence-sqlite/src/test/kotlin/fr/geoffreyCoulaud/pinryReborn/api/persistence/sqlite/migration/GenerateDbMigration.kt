package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite.migration

import io.ebean.annotation.Platform
import io.ebean.dbmigration.DbMigration

/**
 * Generates database migration scripts.
 *
 * Run with: ./gradlew :persistence-sqlite:generateDbMigration
 * The migration files will be created in src/main/resources/dbmigration/
 */
fun main() {
    val migration = DbMigration.create()

    migration.setPlatform(Platform.SQLITE)
    migration.setPathToResources("api-persistence-sqlite/src/main/resources")
    migration.setMigrationPath("dbmigration")

    migration.generateMigration()
}
