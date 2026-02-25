import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.dependency.management) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
}

val targetJavaVersion = libs.versions.java.get()

subprojects {
    // Применяем плагины
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    group = "com.cryptoalert"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    // ИСПРАВЛЕНИЕ ЗДЕСЬ: Используем configure<JavaPluginExtension> вместо java {}
    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
    }

    // Настройка компиляции Kotlin
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = targetJavaVersion
        }
    }

    // Настройка тестов
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // Настройка Detekt
    detekt {
        buildUponDefaultConfig = true
        allRules = false
        // Раскомментируем, когда файл будет создан физически
         config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }
}
