package com.cryptoalert.marketdata.application

import com.cryptoalert.BaseIntegrationTest
import com.cryptoalert.marketdata.domain.CryptoPrice
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset

class MarketDataServiceIntegrationTest(
    private val marketDataService: MarketDataService
): BaseIntegrationTest() {
    init {
        context("MarketDataService Integration") {

            should("save and retrieve price from database") {
                // Arrange
                val symbol = "BTCUSDT"
                val newPrice = CryptoPrice(
                    symbol = symbol,
                    price = BigDecimal("65000.50"),
                    updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
                )

                // Act
                marketDataService.savePrice(newPrice)
                val retrievedPrice = marketDataService.getPrice(symbol)

                // Assert
                retrievedPrice.shouldNotBeNull()
                retrievedPrice.symbol shouldBe symbol
                retrievedPrice.price.shouldBeEqualComparingTo(BigDecimal("65000.50"))
            }
        }
    }
}
