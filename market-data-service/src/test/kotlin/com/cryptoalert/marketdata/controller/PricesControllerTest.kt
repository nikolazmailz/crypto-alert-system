package com.cryptoalert.marketdata.controller

import com.cryptoalert.marketdata.MarketDataBaseIntegrationTest
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import org.springframework.http.ProblemDetail
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.URI

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
                                .withBody("""{"symbol": "$symbol", "price": "50230.45"}"""),
                        ),
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

            should("return ResourceNotFoundException") {
                val response = webTestClient.get()
                    .uri("/api/v1/market-data/prices/NONEXISTENT")
                    .exchange()
                    .expectStatus().isNotFound // Или другой ожидаемый статус
                    .expectBody(ProblemDetail::class.java) // Указываем тип явно
                    .returnResult()
                    .responseBody

                // Теперь можно проверять поля объекта
                response?.type shouldBe URI("https://crypto-alert-system.com/errors/not-found")
                response?.title shouldBe "Not Found"
                response?.status shouldBe 404
                response?.detail shouldBe "Crypto pair NONEXISTENT not found"
                response?.instance shouldBe URI("/api/v1/market-data/prices/NONEXISTENT")
            }
        }
    }
}
