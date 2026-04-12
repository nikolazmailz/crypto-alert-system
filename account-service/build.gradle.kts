import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.openapi.generator)
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(project(":shared-kernel"))

    implementation(libs.postgresql.r2dbc)

    // Spring Security (WebFlux / Reactive)
    implementation(libs.spring.boot.starter.security)

    // JWT
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Тестирование
    testImplementation(testFixtures(project(":shared-kernel")))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

// Настройка генератора OpenAPI
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/api/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath })

    apiPackage.set("com.cryptoalert.account.api")
    modelPackage.set("com.cryptoalert.account.dto")

    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "reactive" to "true",
            "useCoroutines" to "true",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "enumPropertyNaming" to "UPPERCASE",
            "useTags" to "true",       // Tag → имя интерфейса: Auth → AuthApi, Accounts → AccountsApi
            "apiNameSuffix" to "Api",
        ),
    )
}

// Добавляем сгенерированный код в source sets
sourceSets {
    main {
        kotlin.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin").map { it.asFile.absolutePath })
    }
}

// Генерация кода должна произойти ДО компиляции Kotlin
tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.openApiGenerate)
}
