package com.cryptoalert.marketdata.infrastructure.exchangerate

import com.cryptoalert.marketdata.domain.ExchangeRateProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.math.BigDecimal

@Component
class BinanceExchangeRateClient(
    private val binanceWebClient: WebClient,
) : ExchangeRateProvider {
    private val log = KotlinLogging.logger {}

    override suspend fun fetchPrice(symbol: String): BigDecimal? {
        return try {
            val response = binanceWebClient.get()
                // Используем /api/v3/ticker/price из публичного API Binance
                .uri("/api/v3/ticker/price?symbol={symbol}", symbol.uppercase())
                .retrieve()
                .awaitBodyOrNull<BinancePriceResponse>() // Корутиновая функция из WebFlux

            response?.price
        } catch (e: Exception) {
            // В случае сетевой ошибки просто логируем и возвращаем null
            // (позже добавим CircuitBreaker/Retries)
            log.error { "Failed to fetch price for $symbol from Binance $e" }
            null
        }
    }
}
