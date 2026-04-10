package com.cryptoalert.marketdata.application

import com.cryptoalert.marketdata.domain.SymbolInfoProvider
import com.cryptoalert.marketdata.domain.SymbolRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Use-case: периодическая синхронизация торговых пар из Binance в БД.
 *
 * Планировщик запускает задачу раз в 24 часа (настраивается через symbols.sync.interval-ms).
 * Первый запуск происходит при старте приложения (initialDelay = 0 по умолчанию).
 *
 * [applicationScope] — инжектируемый CoroutineScope с SupervisorJob, определён
 * в [com.cryptoalert.marketdata.config.SchedulingConfig]. Позволяет избежать GlobalScope.
 */
@Service
@Profile("!test")
class SymbolSyncService(
    private val symbolInfoProvider: SymbolInfoProvider,
    private val symbolRepository: SymbolRepository,
    private val applicationScope: CoroutineScope,
) {

    private val log = KotlinLogging.logger {}

    /**
     * Точка входа из планировщика Spring.
     * Не является suspend-функцией — просто запускает корутину в managaged scope.
     */
    @Scheduled(fixedRateString = "\${symbols.sync.interval-ms:86400000}")
    fun scheduledSync() {
        log.info { "Triggering scheduled symbols sync" }
        applicationScope.launch {
            runCatching { syncSymbols() }
                .onFailure { log.error(it) { "Symbols sync failed" } }
        }
    }

    /**
     * Основная логика синхронизации: запрашивает список пар у Binance,
     * затем выполняет UPSERT в таблицу symbols.
     */
    suspend fun syncSymbols() {
        log.info { "Fetching exchange info from Binance" }
        val symbols = symbolInfoProvider.fetchExchangeInfo()

        if (symbols.isEmpty()) {
            log.warn { "Received empty symbol list from Binance — skipping sync" }
            return
        }

        log.info { "Syncing ${symbols.size} symbols to database" }
        symbolRepository.upsertAll(symbols)
        log.info { "Symbols sync completed successfully" }
    }
}
