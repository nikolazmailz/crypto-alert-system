package com.cryptoalert.notification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
//import org.springframework.boot.autoconfigure.r2dbc.R2dbcRepositoriesAutoConfiguration
import org.springframework.boot.runApplication

/**
 * Standalone entry point for the notification-dispatcher microservice.
 *
 * Database-related auto-configurations are excluded because this module
 * has no persistence layer — it is a pure delivery gateway.
 */
@SpringBootApplication(
    scanBasePackages = ["com.cryptoalert.shared", "com.cryptoalert.notification"],
    exclude = [
        DataSourceAutoConfiguration::class,
        R2dbcAutoConfiguration::class,
//        R2dbcRepositoriesAutoConfiguration::class,
        LiquibaseAutoConfiguration::class,
    ],
)
class NotificationApplication

fun main(args: Array<String>) {
    runApplication<NotificationApplication>(*args)
}
