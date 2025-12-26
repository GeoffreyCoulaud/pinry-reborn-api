plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.quarkus)
}

dependencies {
    // All modules
    implementation(project(":api-domain"))
    implementation(project(":api-usecases"))
    implementation(project(":api-persistence-sqlite"))
    implementation(project(":api-presentation-quarkus"))

    // Quarkus platform
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.kotlin)
    implementation(libs.quarkus.rest)
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.kotlin.stdlib)
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
