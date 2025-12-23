rootProject.name = "pinry-reborn"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus") version "3.30.4"
        kotlin("jvm") version "2.2.21"
        kotlin("kapt") version "2.2.21"
        kotlin("plugin.allopen") version "2.2.21"
        kotlin("plugin.noarg") version "2.2.21"
        id("io.ebean") version "17.2.0"
        id("org.kordamp.gradle.jandex") version "2.1.0"
    }
}

include(":api-domain")
include(":api-usecases")
include(":api-persistence-sqlite")
include(":api-presentation-rest")
include(":api-application")
