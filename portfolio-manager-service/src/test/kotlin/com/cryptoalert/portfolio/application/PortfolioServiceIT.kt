package com.cryptoalert.portfolio.application

import com.cryptoalert.portfolio.PortfolioBaseIntegrationTest
import com.cryptoalert.portfolio.domain.Portfolio
import com.cryptoalert.portfolio.domain.PortfolioRepository
import com.cryptoalert.shared.cached.CaffeineStampedeCache
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class PortfolioServiceIT(
    private val portfolioService: PortfolioService,
    private val priceCache: CaffeineStampedeCache<String, BigDecimal>,
    private val portfolioRepository: PortfolioRepository,
): PortfolioBaseIntegrationTest() {

    private val _callCount = AtomicInteger(0)
    val callCount: Int get() = _callCount.get()

    init {

        beforeTest {
            _callCount.set(0)
            priceCache.invalidateAll()
            deleteAllPortfolios()

            coEvery { priceProvider.getCurrentPrice(any()) } coAnswers { call ->
                _callCount.incrementAndGet()
                delay(50)
                println("getPrice called with: $callCount")
                // возвращаем нужное значение
                BigDecimal.valueOf(5054.2)
            }
        }


        // ──────────────────────────────────────────────────
        // Сценарий 1: Cache Stampede
        // ──────────────────────────────────────────────────
        context("Cache Stampede — 10 параллельных запросов на один символ") {

            should("вызвать priceProvider ровно 1 раз для одного символа") {
                val userId = UUID.randomUUID()
                portfolioRepository.save(
                    Portfolio(userId = userId).apply {
                        addAsset("BTC", BigDecimal.ONE)
                    }
                )

                coroutineScope {
                    (1..10).map {
                        async { portfolioService.calculateTotalValue(userId) }
                    }.awaitAll()
                }

                callCount shouldBe 1
                coVerify(exactly = 1) { priceProvider.getCurrentPrice("BTC") }
            }

            should("вызвать priceProvider по одному разу на каждый уникальный символ") {
                val userId = UUID.randomUUID()
                portfolioRepository.save(
                    Portfolio(userId = userId).apply {
                        addAsset("BTC", BigDecimal.ONE)
                        addAsset("ETH", BigDecimal.TEN)
                        addAsset("SOL", BigDecimal.valueOf(5))
                    }
                )

                coroutineScope {
                    (1..10).map {
                        async { portfolioService.calculateTotalValue(userId) }
                    }.awaitAll()
                }

                // 3 символа → ровно 3 вызова, несмотря на 10 параллельных запросов
                callCount shouldBe 3
                coVerify(exactly = 1) { priceProvider.getCurrentPrice("BTC") }
                coVerify(exactly = 1) { priceProvider.getCurrentPrice("ETH") }
                coVerify(exactly = 1) { priceProvider.getCurrentPrice("SOL") }
            }
        }


        // ──────────────────────────────────────────────────
        // Сценарий 2: Кэш работает — повторный вызов не идёт в priceProvider
        // ──────────────────────────────────────────────────
        context("Кэш — повторный вызов не идёт в priceProvider") {

            should("не вызывать priceProvider при повторном calculateTotalValue") {
                val userId = UUID.randomUUID()
                portfolioRepository.save(
                    Portfolio(userId = userId).apply {
                        addAsset("BTC", BigDecimal.ONE)
                    }
                )

                portfolioService.calculateTotalValue(userId) // прогрев кэша
                callCount shouldBe 1

                portfolioService.calculateTotalValue(userId) // должен взять из кэша

                callCount shouldBe 1
                coVerify(exactly = 1) { priceProvider.getCurrentPrice("BTC") }
            }

            should("корректно посчитать сумму портфеля") {
                val userId = UUID.randomUUID()
                portfolioRepository.save(
                    Portfolio(userId = userId).apply {
                        addAsset("BTC", BigDecimal.valueOf(2))
                    }
                )

                val total = portfolioService.calculateTotalValue(userId)

                // 2 BTC * 5054.2 = 10108.4
                total shouldBe BigDecimal.valueOf(5054.2).multiply(BigDecimal.valueOf(2))
            }
        }

    }
}
