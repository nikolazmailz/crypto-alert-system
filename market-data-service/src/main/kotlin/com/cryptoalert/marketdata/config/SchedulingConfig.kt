package com.cryptoalert.marketdata.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Активирует Spring Scheduler и предоставляет application-level CoroutineScope.
 *
 * SupervisorJob гарантирует, что падение одной дочерней корутины (например, одного запуска
 * syncSymbols) не отменяет остальные и не убивает весь scope.
 *
 * Scope инжектируется в [com.cryptoalert.marketdata.application.SymbolSyncService],
 * чтобы избежать GlobalScope (запрещён по CLAUDE.md).
 */
@Configuration
@EnableScheduling
class SchedulingConfig {

    @Bean
    fun applicationCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
