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

    // Добавляем WebFlux в ядро (api, чтобы другие модули его наследовали)
    api(libs.spring.boot.starter.webflux)

    // logging
    api(libs.oshai.kotlin.logging)

    // Kotlin Coroutines (Мост между Project Reactor (Spring WebFlux/R2DBC) и Kotlin Coroutines)
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

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false // Отключаем создание исполняемого Spring Boot JAR
}

tasks.getByName<Jar>("jar") {
    enabled = true // Включаем создание обычного JAR, который можно подключать как зависимость
}
