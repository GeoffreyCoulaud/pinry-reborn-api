plugins {
    kotlin("jvm")
    id("org.kordamp.gradle.jandex")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":api-domain"))
    implementation(project(":api-utilities"))

    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")

    testImplementation(testFixtures(project(":api-utilities")))
    testImplementation("io.mockk:mockk:1.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
