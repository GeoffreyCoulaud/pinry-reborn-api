package fr.geoffreyCoulaud.pinryReborn.api.persistence.sqlite

import io.ebean.Database
import io.ebean.DatabaseFactory
import io.ebean.config.DatabaseConfig
import io.ebean.datasource.DataSourceConfig
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Singleton

@ApplicationScoped
class EbeanDatabaseProducer {
    @Produces
    @Singleton
    fun produceDatabase(): Database {
        val dbPath = System.getenv("DB_PATH") ?: "data.db"

        val dataSourceConfig = DataSourceConfig()
        dataSourceConfig.url = "jdbc:sqlite:$dbPath"
        dataSourceConfig.driver = "org.sqlite.JDBC"
        dataSourceConfig.username = "sa"
        dataSourceConfig.password = ""

        val config = DatabaseConfig()
        config.setDefaultServer(true)
        config.setDataSourceConfig(dataSourceConfig)
        config.setDdlGenerate(false)
        config.setDdlRun(false)
        config.setRunMigration(true)
        config.setPackages(listOf("fr.geoffreyCoulaud.pinryReborn.adapters.persistence.models"))

        return DatabaseFactory.create(config)
    }
}
