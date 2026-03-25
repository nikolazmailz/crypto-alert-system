package com.cryptoalert.marketdata.controller

import com.cryptoalert.marketdata.MarketDataBaseIntegrationTest
import com.cryptoalert.marketdata.domain.CryptoPriceRepository
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.extensions.spring.SpringExtension
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient

class PricesControllerTest(
    private val webTestClient: WebTestClient,
    private val databaseClient: DatabaseClient,
) : MarketDataBaseIntegrationTest(databaseClient) {

    init {
        beforeTest {
            runBlocking {
                deleteAllCryptoPrices()
            }
        }

        context("GET /api/v1/market-data/prices/{symbol}") {

            should("return 200 OK and correct stub price for BTCUSDT") {
                val symbol = "BTCUSDT"
                wireMockServer.stubFor(
                    get(urlEqualTo("/api/v3/ticker/price?symbol=$symbol"))
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("""{"symbol": "$symbol", "price": "50230.45"}""")
                        )
                )

                webTestClient.get()
                    .uri("/api/v1/market-data/prices/BTCUSDT")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.symbol").isEqualTo("BTCUSDT")
                    .jsonPath("$.price").isEqualTo(50230.45)
                    .jsonPath("$.updatedAt").isNotEmpty
            }
        }
    }
}
