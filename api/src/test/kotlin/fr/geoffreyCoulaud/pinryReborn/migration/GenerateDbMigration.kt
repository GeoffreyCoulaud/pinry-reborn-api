package fr.geoffreyCoulaud.pinryReborn.migration

import io.ebean.annotation.Platform
import io.ebean.dbmigration.DbMigration

/**
 * Generates database migration scripts.
 *
 * Run this main function to generate a new migration when entities change.
 * The migration files will be created in src/main/resources/dbmigration/
 */
fun main() {
    val migration = DbMigration.create()

    migration.setPlatform(Platform.SQLITE)
    migration.setPathToResources("src/main/resources")
    migration.setMigrationPath("dbmigration")

    // Generate the migration
    migration.generateMigration()
}
