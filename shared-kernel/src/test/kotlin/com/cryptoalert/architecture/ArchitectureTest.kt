package com.cryptoalert.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import io.kotest.core.spec.style.ShouldSpec

class ArchitectureTest : ShouldSpec() {

    private val importedClasses = ClassFileImporter().importPackages("com.cryptoalert")

    init {

        should("layers should respect clean architecture") {
            layeredArchitecture()
                .consideringAllDependencies()
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .layer("Controller").definedBy("..controller..")
                .withOptionalLayers(true)
                // Правила зависимостей
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Controller")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Controller")
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Controller")
                .check(importedClasses)
        }

        should("modules should not depend on each other internals") {
            // Здесь мы будем добавлять правила для изоляции модулей,
            // например, market-data не должен знать про внутренности portfolio-manager
        }
    }
}
