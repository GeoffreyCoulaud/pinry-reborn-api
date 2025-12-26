plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-test-fixtures`
}

dependencies {
    // Test fixtures dependencies (shared test utilities)
    testFixturesImplementation(libs.bundles.testing)

    // Test dependencies
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.bundles.testing.runtime)
}
