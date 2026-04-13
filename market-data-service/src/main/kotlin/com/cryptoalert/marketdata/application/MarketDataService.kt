package com.cryptoalert.marketdata.application

import com.cryptoalert.marketdata.api.PriceProvider
import com.cryptoalert.marketdata.domain.CryptoPrice
import com.cryptoalert.marketdata.domain.CryptoPriceRepository
import com.cryptoalert.marketdata.domain.ExchangeRateProvider
import com.cryptoalert.shared.error.ResourceNotFoundException
import com.cryptoalert.shared.event.PriceChangedEvent
import com.cryptoalert.shared.event.PriceEventPublisher
import com.cryptoalert.shared.observability.observeSuspend
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Application-слой для market-data.
 *
 * Инструментация latency обновления цены:
 *  Observation [METRIC_PRICE_UPDATE_LATENCY] охватывает весь путь «внешний API →
 *  запись в БД → публикация события» и записывает время этой операции.
 *
 *  Prometheus-метрика: `crypto_price_update_latency_seconds_{count,sum,max,bucket}`
 *  с тегом `symbol`. Гистограмма включена в application.yml (percentiles-histogram).
 *
 *  Кэш-хит (данные уже есть в БД) не инструментируется — это fast path,
 *  и его latency не интересна с точки зрения бизнеса.
 */
@Service
class MarketDataService(
    private val repository: CryptoPriceRepository,
    private val exchangeRateProvider: ExchangeRateProvider,
    private val eventPublisher: PriceEventPublisher,
    private val observationRegistry: ObservationRegistry,
) : PriceProvider {

    suspend fun savePrice(cryptoPrice: CryptoPrice): CryptoPrice =
        repository.save(cryptoPrice)

    suspend fun getPrice(symbol: String): CryptoPrice {
        val normalizedSymbol = symbol.uppercase()

        // Кэш-хит: возвращаем из БД без внешнего запроса и инструментации.
        return repository.findById(normalizedSymbol)
            ?: fetchFromExchangeAndPublish(normalizedSymbol)
    }

    /**
     * Вызывается только при cache-miss: идёт во внешний API, сохраняет и
     * публикует событие. Именно этот путь инструментируется как "latency
     * обработки обновления цены" согласно требованиям.
     */
    private suspend fun fetchFromExchangeAndPublish(symbol: String): CryptoPrice =
        Observation.createNotStarted(METRIC_PRICE_UPDATE_LATENCY, observationRegistry)
            // symbol — low cardinality: конечный набор торговых пар (BTCUSDT, ETHUSDT…)
            .lowCardinalityKeyValue("symbol", symbol)
            .observeSuspend {
                val livePrice = exchangeRateProvider.fetchPrice(symbol)
                    ?: throw ResourceNotFoundException("Crypto pair $symbol not found")

                val newPrice = CryptoPrice(
                    symbol = symbol,
                    price = livePrice,
                    updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
                )
                // Публикация события — внутри Observation, чтобы задержка
                // распространения события тоже учитывалась в метрике.
                eventPublisher.publish(PriceChangedEvent(symbol, newPrice.price))
                repository.save(newPrice)
            }

    override suspend fun getCurrentPrice(symbol: String): BigDecimal =
        getPrice(symbol).price

    companion object {
        /**
         * Имя Observation → Prometheus-метрики:
         *   `crypto_price_update_latency_seconds_bucket` (гистограмма для p50/p95/p99)
         *   `crypto_price_update_latency_seconds_count`  (количество обновлений)
         *   `crypto_price_update_latency_seconds_sum`    (суммарное время)
         *   `crypto_price_update_latency_seconds_max`    (максимальное время, 2-мин окно)
         */
        private const val METRIC_PRICE_UPDATE_LATENCY = "crypto.price.update.latency"
    }
}
