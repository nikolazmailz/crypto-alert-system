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
    implementation(project(":market-data-service"))
    implementation(project(":shared-kernel"))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.openapi)

    // DB
    implementation(libs.postgresql.r2dbc)

    // Resilience
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.kotlin)

    // Swagger
    implementation(libs.openapi)

    testImplementation(testFixtures(project(":shared-kernel")))
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

// Настройка генератора OpenAPI
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/api/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath })

    apiPackage.set("com.cryptoalert.portfolio.controller")
    modelPackage.set("com.cryptoalert.portfolio.controller.dto")

    configOptions.set(mapOf(
        "useSpringBoot3" to "true",
        "reactive" to "true",         // Поддержка WebFlux (Flux/Mono)
        "useCoroutines" to "true",    // Превращает Flux/Mono в suspend и Flow
        "interfaceOnly" to "true",    // Генерируем только интерфейсы, реализацию пишем сами
        "skipDefaultInterface" to "true",
        "enumPropertyNaming" to "UPPERCASE",
        "useTags" to "true",            // Использовать теги для генерации имен
        "apiNameSuffix" to "Api"        // Суффикс. Итого будет: {Tag} + {Suffix} = PortfolioApi
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

