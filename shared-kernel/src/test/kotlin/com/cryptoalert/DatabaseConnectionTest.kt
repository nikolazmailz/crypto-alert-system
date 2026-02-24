package com.cryptoalert

import io.kotest.matchers.shouldBe
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOne

class DatabaseConnectionTest(
    private val databaseClient: DatabaseClient // Kotest SpringExtension автоматически заинжектит бин
) : BaseIntegrationTest() {

    init {
        context("PostgreSQL Testcontainers Connection") {

            should("execute SELECT 1 successfully via R2DBC") {
                // Act
                val result = databaseClient.sql("SELECT 1 AS result")
                    .fetch()
                    .awaitOne()

                // Assert
                result["result"] shouldBe 1
            }
        }
    }
}
