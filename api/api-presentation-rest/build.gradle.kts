plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    id("org.kordamp.gradle.jandex")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":api-domain"))
    implementation(project(":api-usecases"))
    implementation(project(":api-utilities"))

    // Quarkus RESTEasy Reactive - provided by Quarkus at runtime
    compileOnly("io.quarkus.resteasy.reactive:resteasy-reactive-common:3.30.4")

    // Jakarta APIs - implementation provided by Quarkus at runtime
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    compileOnly("jakarta.inject:jakarta.inject-api:2.0.1")
    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")

    // Tests
    testImplementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    testImplementation("io.quarkus.resteasy.reactive:resteasy-reactive-common:3.30.4")
    testImplementation("io.quarkus.resteasy.reactive:resteasy-reactive:3.30.4")
    testImplementation("io.mockk:mockk:1.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
