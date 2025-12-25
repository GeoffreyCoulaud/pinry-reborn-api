plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
}

dependencies {
    // All modules
    implementation(project(":api-domain"))
    implementation(project(":api-usecases"))
    implementation(project(":api-persistence-sqlite"))
    implementation(project(":api-presentation-quarkus"))

    // Quarkus platform
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.30.4"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-quarkus")
    implementation("io.quarkus:quarkus-quarkus-jackson")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
