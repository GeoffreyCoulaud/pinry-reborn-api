plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    id("io.ebean")
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":api-domain"))
    implementation(project(":api-utilities"))

    // Ebean ORM
    implementation("io.ebean:ebean:17.2.0")
    implementation("io.ebean:ebean-sqlite:17.2.0")
    implementation("io.ebean:ebean-ddl-generator:17.2.0")
    implementation("io.ebean:ebean-migration:13.6.0")
    kapt("io.ebean:kotlin-querybean-generator:17.2.0")

    // SQLite JDBC driver
    implementation("org.xerial:sqlite-jdbc:3.47.2.0")

    // CDI API for annotations - implementation provided by Quarkus at runtime
    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")

    testImplementation("io.ebean:ebean-test:17.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

ebean {
    debugLevel = 1
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JavaExec>("generateDbMigration") {
    group = "ebean"
    description = "Generate Ebean database migration"
    mainClass.set("fr.geoffreyCoulaud.pinryReborn.migration.GenerateDbMigrationKt")
    classpath = sourceSets["test"].runtimeClasspath
    dependsOn("compileTestKotlin")
}
