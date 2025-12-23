package fr.geoffreyCoulaud.pinryReborn.adapters.persistence

import io.ebean.Database
import io.ebean.DatabaseFactory
import io.ebean.config.DatabaseConfig
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Singleton

@ApplicationScoped
class EbeanDatabaseProducer {

    @Produces
    @Singleton
    fun produceDatabase(): Database {
        val config = DatabaseConfig()
        config.isDefaultServer = true
        config.loadFromProperties()
        return DatabaseFactory.create(config)
    }
}