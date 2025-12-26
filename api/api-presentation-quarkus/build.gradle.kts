plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.jandex)
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
}

dependencies {
    implementation(project(":api-domain"))
    implementation(project(":api-usecases"))
    implementation(project(":api-utilities"))

    // Quarkus RESTEasy Reactive - provided by Quarkus at runtime
    compileOnly(libs.resteasy.reactive.common)

    // Jakarta APIs - implementation provided by Quarkus at runtime
    compileOnly(libs.jakarta.ws.rs.api)
    compileOnly(libs.jakarta.inject.api)
    compileOnly(libs.jakarta.cdi.api)

    // Tests
    testImplementation(testFixtures(project(":api-utilities")))
    testImplementation(libs.jakarta.ws.rs.api)
    testImplementation(libs.resteasy.reactive.common)
    testImplementation(libs.resteasy.reactive)
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.bundles.testing.runtime)
}
