package com.cryptoalert.marketdata.infrastructure.exchangerate

import com.cryptoalert.marketdata.domain.ExchangeRateProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.decorateSuspendFunction
import io.github.resilience4j.kotlin.retry.decorateSuspendFunction
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.math.BigDecimal

@Component
class BinanceExchangeRateClient(
    private val binanceWebClient: WebClient,
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val retryRegistry: RetryRegistry
) : ExchangeRateProvider {
    private val log = KotlinLogging.logger {}

//    @CircuitBreaker(name = "binanceClient")
//    @Retry(name = "binanceClient")
//    override suspend fun fetchPrice(symbol: String): BigDecimal? {
//        return try {
//            binanceWebClient.get()
//                // Используем /api/v3/ticker/price из публичного API Binance
//                .uri("/api/v3/ticker/price?symbol={symbol}", symbol.uppercase())
//                .retrieve()
//                .awaitBodyOrNull<BinancePriceResponse>()
//                ?.price
//        } catch (e: WebClientException) {
//            // В случае сетевой ошибки просто логируем и возвращаем null
//            // (позже добавим CircuitBreaker/Retries)
//            log.error { "Failed to fetch price for $symbol from Binance $e" }
//            null
//        }
//    }

    override suspend fun fetchPrice(symbol: String): BigDecimal? {
        val cb = circuitBreakerRegistry.circuitBreaker("binanceClient")
        val retry = retryRegistry.retry("binanceClient")

        val decoratedCall: suspend () -> BigDecimal? = {
            binanceWebClient.get()
                .uri("/api/v3/ticker/price?symbol={symbol}", symbol.uppercase())
                .retrieve()
                .awaitBodyOrNull<BinancePriceResponse>()?.price
        }

        return try {
            retry.decorateSuspendFunction(cb.decorateSuspendFunction(decoratedCall)).invoke()
        } catch (e: WebClientException) {
            log.error { "Failed to fetch price for $symbol from Binance $e" }
            null
        }
    }
}
