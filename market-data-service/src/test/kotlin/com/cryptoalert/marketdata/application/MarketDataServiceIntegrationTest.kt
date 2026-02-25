package com.cryptoalert.marketdata.application

import com.cryptoalert.BaseIntegrationTest
import com.cryptoalert.marketdata.domain.CryptoPrice
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
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

            should("fetch price from external API (WireMock) when not found in DB") {
                // Arrange: Настраиваем WireMock отдавать 3500.50 для ETHUSDT
                val symbol = "ETHUSDT"
                wireMockServer.stubFor(
                    get(urlEqualTo("/api/v3/ticker/price?symbol=$symbol"))
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("""{"symbol": "$symbol", "price": "3500.50"}""")
                        )
                )

                // Act: Запрашиваем цену (в БД пусто, поэтому пойдет по сети)
                val retrievedPrice = marketDataService.getPrice(symbol)

                // Assert: Проверяем, что вернулась правильная цена
                retrievedPrice.shouldNotBeNull()
                retrievedPrice.price.shouldBeEqualComparingTo(BigDecimal("3500.50"))

                // Убеждаемся, что цена действительно сохранилась в БД
                val priceFromDb = marketDataService.getPrice(symbol)
                priceFromDb.shouldNotBeNull()
                priceFromDb.price.shouldBeEqualComparingTo(BigDecimal("3500.50"))
            }
        }
    }
}
