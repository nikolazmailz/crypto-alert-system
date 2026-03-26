package com.cryptoalert.marketdata.architecture

import com.cryptoalert.architecture.SharedArchitectureRules
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import io.kotest.core.spec.style.ShouldSpec

class MarketDataArchitectureTest: ShouldSpec() {

    private val importedClasses = ClassFileImporter().importPackages("com.cryptoalert.marketdata")

    private val allProjectClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.cryptoalert")

    init {

        should("should respect shared architecture rules"){
            SharedArchitectureRules.cleanArchitecture().check(importedClasses)
        }

        // todo check
        should("market-data should not depend on portfolio-manager internals") {
            SharedArchitectureRules.moduleIsolation("marketdata", "portfolio")
                .check(allProjectClasses)
        }
    }
}
