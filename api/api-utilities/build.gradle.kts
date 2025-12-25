plugins {
    kotlin("jvm")
    `java-test-fixtures`
}

group = "fr.geoffreyCoulaud.pinryReborn"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Test fixtures dependencies (shared test utilities)
    testFixturesImplementation("io.mockk:mockk:1.14.0")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.11.0")

    // Test dependencies
    testImplementation("io.mockk:mockk:1.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
