package com.cryptoalert.marketdata.controller

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [PricesController::class])
class PricesControllerTest(
    private val webClient: WebTestClient
) : ShouldSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        context("GET /api/v1/market-data/prices/{symbol}") {

            should("return 200 OK and correct stub price for BTCUSDT") {
                webClient.get()
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
