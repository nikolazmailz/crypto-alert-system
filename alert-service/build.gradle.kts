import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.openapi.generator)
}

dependencies {
    implementation(kotlin("reflect"))
    // Модуль зависит от общего ядра
    implementation(project(":shared-kernel"))

    implementation(libs.postgresql.r2dbc)
    implementation(libs.spring.kafka)

    // JWT-безопасность: spring-security + jjwt.
    // JwtSecurityAutoConfiguration из shared-kernel автоматически создаст
    // JwtSecurityService, JwtReactiveAuthenticationManager, JwtSecurityContextRepository
    // при наличии этих зависимостей и jwt.secret в application.yml.
    implementation(libs.spring.boot.starter.security)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // ДОБАВЛЯЕМ подключение тестовых фикстур из ядра для наших тестов
    testImplementation(testFixtures(project(":shared-kernel")))
    // Тестирование
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extensions.spring)
}

// Настройка генератора OpenAPI
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/api/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath })

    apiPackage.set("com.cryptoalert.alert.api")
    modelPackage.set("com.cryptoalert.alert.dto")

    configOptions.set(mapOf(
        "useSpringBoot3" to "true",
        "reactive" to "true",         // Поддержка WebFlux (Flux/Mono)
        "useCoroutines" to "true",    // Превращает Flux/Mono в suspend и Flow
        "interfaceOnly" to "true",    // Генерируем только интерфейсы, реализацию пишем сами
        "skipDefaultInterface" to "true",
        "enumPropertyNaming" to "UPPERCASE",
        "useTags" to "true",            // Использовать теги для генерации имен
        "apiNameSuffix" to "Api"        // Суффикс. Итого будет: {Tag} + {Suffix} = AlertApi
    ))
}

// Добавляем сгенерированный код в source sets, чтобы IDE и компилятор его видели
sourceSets {
    main {
        kotlin.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin").map { it.asFile.absolutePath })
    }
}

// Гарантируем, что генерация кода произойдет ДО компиляции Kotlin
tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.openApiGenerate)
}

