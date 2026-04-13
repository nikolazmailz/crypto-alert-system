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
    api(libs.spring.boot.starter.validation)

    // Зависимости для сгенерированного кода (Аннотации Swagger и Валидация)
    api(libs.swagger.annotations)
    api(libs.jakarta.validation)

    // logging
    api(libs.oshai.kotlin.logging)

    // Kotlin Coroutines (Мост между Project Reactor (Spring WebFlux/R2DBC) и Kotlin Coroutines)
    api(libs.kotlinx.coroutines.reactor)

    // Resilience
    api(libs.resilience4j.spring.boot3)
    api(libs.resilience4j.kotlin)
    api(libs.resilience4j.reactor)

    // jackson
    api(libs.koltin.jackson.module)
    api(libs.jackson.datatype.jsr310)

    // Swagger (OpenApi)
    api(libs.openapi)

    // Caffeine
    api(libs.caffeine)

    // ── Monitoring & Observability ────────────────────────────────────────────
    // api(), а не implementation — чтобы все зависимые модули получали их
    // транзитивно и не дублировали объявления в своих build.gradle.kts.
    api(libs.spring.boot.starter.actuator)
    api(libs.micrometer.registry.prometheus)
    // Мост к OpenTelemetry — нужен для будущей интеграции с OTel-коллектором.
    // Без реального экспортёра трейсы просто отбрасываются, ошибок нет.
    api(libs.micrometer.tracing.bridge.otel)
    // Kotlin-расширения Micrometer (observeSuspend и др.)
    api(libs.micrometer.kotlin)

    // JJWT — compileOnly. Нужен для компиляции JwtSecurityService.
    // Сервисы, которым нужна JWT-валидация, добавляют jjwt-impl и jjwt-jackson сами.
    compileOnly(libs.jjwt.api)

    // Spring Security — compileOnly, НЕ api/implementation.
    // Нужен только для компиляции SecurityContextUtils.kt.
    // Не должен попадать в transitive-зависимости других модулей:
    // market-data-service, alert-service и т.д. НЕ должны получать
    // spring-security на classpath автоматически, иначе Spring Boot
    // AutoConfiguration активирует защиту на ВСЕХ эндпоинтах.
    // Каждый сервис, которому нужна security, добавляет её ЯВНО сам.
    compileOnly(libs.spring.boot.starter.security)

    // Test dependencies
    testFixturesApi(libs.spring.boot.starter.test)
    testFixturesApi(libs.testcontainers.postgresql)
    testFixturesApi(libs.kotest.runner.junit5)
    testFixturesApi(libs.kotest.assertions.core)
    testFixturesApi(libs.kotest.extensions.spring)
    testFixturesApi(libs.wiremock)
    testFixturesApi(libs.archunit)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.archunit)
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
