plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("org.kordamp.gradle.jandex")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
}

dependencies {
    implementation(project(":api-domain"))
    implementation(project(":api-usecases"))

    // Jakarta APIs - implementation provided by Quarkus at runtime
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    compileOnly("jakarta.inject:jakarta.inject-api:2.0.1")
    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")
}
