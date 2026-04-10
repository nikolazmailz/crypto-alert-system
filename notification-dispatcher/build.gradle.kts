plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(kotlin("reflect"))

    // Common infrastructure: WebFlux, Coroutines, Jackson, logging, etc.
    implementation(project(":shared-kernel"))

    // E-mail delivery via SMTP
    implementation(libs.spring.boot.starter.mail)

    // Kafka consumer (notification-events topic)
    implementation(libs.spring.kafka)

    // ──── Tests ────
    testImplementation(testFixtures(project(":shared-kernel")))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.kotlinx.coroutines.test)
}
