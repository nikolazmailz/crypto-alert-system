plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    // R2DBC (api значит, что другие модули тоже получат эти зависимости)
    api(libs.spring.boot.starter.data.r2dbc)
    runtimeOnly(libs.postgresql.r2dbc)

    // Kotlin Coroutines
    api(libs.kotlinx.coroutines.reactor)

    // Test dependencies
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Указываем Gradle использовать JUnit Platform для запуска Kotest
tasks.withType<Test> {
    useJUnitPlatform()
}
