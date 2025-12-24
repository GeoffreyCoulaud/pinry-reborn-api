plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("org.kordamp.gradle.jandex")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":api-domain"))
    implementation(project(":api-usecases"))
    implementation(project(":api-utilities"))

    // Jakarta APIs - implementation provided by Quarkus at runtime
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    compileOnly("jakarta.inject:jakarta.inject-api:2.0.1")
    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")

    // Tests
    testImplementation("io.mockk:mockk:1.14.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.rest-assured:rest-assured:6.0.0")
    testImplementation("io.rest-assured:kotlin-extensions:6.0.0")
    testImplementation("io.rest-assured:json-path:6.0.0")
}
