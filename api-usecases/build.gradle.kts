plugins {
    kotlin("jvm")
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":api-domain"))

    // CDI API for @ApplicationScoped - implementation provided by Quarkus at runtime
    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")
}
