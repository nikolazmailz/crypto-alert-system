package com.cryptoalert.architecture

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture

object SharedArchitectureRules {
    // Определяем стандартную структуру слоев Clean Architecture
    fun cleanArchitecture() = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Domain").definedBy("..domain..")
        .layer("Application").definedBy("..application..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .layer("Controller").definedBy("..controller..")
        .withOptionalLayers(true)
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Controller")
        .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Controller")
        .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Controller")

    /**
     * Правило: Запрещает текущему модулю зависеть от внутренних частей другого модуля.
     * @param currentModule Имя текущего модуля (например, "marketdata")
     * @param otherModule Имя чужого модуля (например, "portfolio")
     */
    fun moduleIsolation(currentModule: String, otherModule: String) =
        noClasses().that().resideInAPackage("..$currentModule..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..$otherModule.domain..",
                "..$otherModule.application..",
                "..$otherModule.infrastructure.."
            )
            .because("Модуль $currentModule должен использовать только публичный API модуля $otherModule")
}
