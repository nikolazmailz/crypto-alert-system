plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.kotlin.spring)

    id("java-test-fixtures")
}

dependencies {
    // R2DBC
    api(libs.spring.boot.starter.data.r2dbc)
    runtimeOnly(libs.postgresql.r2dbc)

    // Миграции БД (Liquibase работает через JDBC)
    api(libs.spring.boot.starter.jdbc)
    api(libs.liquibase.core)
    runtimeOnly(libs.postgresql.jdbc) // <-- Добавили классический драйвер!

    // Kotlin Coroutines
    api(libs.kotlinx.coroutines.reactor)

    // Test dependencies
    testFixturesApi(libs.spring.boot.starter.test)
    testFixturesApi(libs.testcontainers.postgresql)
    testFixturesApi(libs.kotest.runner.junit5)
    testFixturesApi(libs.kotest.assertions.core)
    testFixturesApi(libs.kotest.extensions.spring)
    testFixturesApi(libs.wiremock)

    testImplementation(libs.kotlinx.coroutines.test)
}

// Указываем Gradle использовать JUnit Platform для запуска Kotest
tasks.withType<Test> {
    useJUnitPlatform()
}
