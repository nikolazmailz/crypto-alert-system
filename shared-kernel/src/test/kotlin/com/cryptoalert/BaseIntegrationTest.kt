package com.cryptoalert

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("test")
abstract class BaseIntegrationTest : ShouldSpec() {

    override fun extensions() = listOf(SpringExtension)

    companion object {
        // Контейнер стартует один раз при загрузке класса в JVM
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("crypto_alert_test")
            withUsername("test")
            withPassword("test")
            withReuse(true) // Позволяет не перезапускать БД при локальной разработке
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            // Формируем реактивный URL для R2DBC
            val r2dbcUrl = "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"

            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)
        }
    }
}
