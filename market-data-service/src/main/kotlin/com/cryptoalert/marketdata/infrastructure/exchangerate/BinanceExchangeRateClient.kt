package com.cryptoalert.marketdata.infrastructure.exchangerate

import com.cryptoalert.marketdata.domain.ExchangeRateProvider
import com.cryptoalert.marketdata.domain.Symbol
import com.cryptoalert.marketdata.domain.SymbolInfoProvider
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
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class BinanceExchangeRateClient(
    private val binanceWebClient: WebClient,
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val retryRegistry: RetryRegistry,
) : ExchangeRateProvider, SymbolInfoProvider {

    private val log = KotlinLogging.logger {}

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
            log.error { "Failed to fetch price for $symbol from Binance: $e" }
            null
        }
    }

    override suspend fun fetchExchangeInfo(): List<Symbol> {
        val cb = circuitBreakerRegistry.circuitBreaker("binanceClient")
        val retry = retryRegistry.retry("binanceClient")

        val now = OffsetDateTime.now(ZoneOffset.UTC)

        val decoratedCall: suspend () -> List<Symbol> = {
            binanceWebClient.get()
                .uri("/api/v3/exchangeInfo")
                .retrieve()
                .awaitBodyOrNull<BinanceExchangeInfoResponse>()
                ?.symbols
                ?.map { it.toDomain(now) }
                ?: emptyList()
        }

        return try {
            retry.decorateSuspendFunction(cb.decorateSuspendFunction(decoratedCall)).invoke()
        } catch (e: WebClientException) {
            log.error { "Failed to fetch exchange info from Binance: $e" }
            emptyList()
        }
    }
}

private fun BinanceSymbolInfo.toDomain(syncedAt: OffsetDateTime): Symbol = Symbol(
    symbol = symbol,
    baseAsset = baseAsset,
    quoteAsset = quoteAsset,
    status = status,
    lastUpdatedAt = syncedAt,
)
