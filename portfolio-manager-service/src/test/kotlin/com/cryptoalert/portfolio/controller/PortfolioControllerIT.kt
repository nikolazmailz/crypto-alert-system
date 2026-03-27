package com.cryptoalert.portfolio.controller

import com.cryptoalert.portfolio.PortfolioBaseIntegrationTest
import com.cryptoalert.portfolio.controller.dto.AddAssetRequest
import com.cryptoalert.portfolio.controller.dto.PortfolioValueResponse
import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import org.springframework.http.MediaType
import java.math.BigDecimal
import java.util.UUID

class PortfolioControllerIT: PortfolioBaseIntegrationTest() {

    init {
        beforeTest {
            runBlocking {
                deleteAllPortfolios()
            }
        }

        context("Portfolio Management Flow") {

            should("successfully add asset and calculate total value") {
                // GIVEN
                val userId = UUID.randomUUID()
                val symbol = "BTCUSDT"
                val quantity = 0.5
                val btcPrice = BigDecimal("60000.0")

                // Мокаем ответ от соседа (market-data-service)
                coEvery { priceProvider.getCurrentPrice(symbol) } returns btcPrice

                // WHEN: Добавляем актив
                webTestClient.post()
                    .uri("/api/v1/portfolios/$userId/assets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(AddAssetRequest(symbol = symbol, quantity = quantity))
                    .exchange()
                    .expectStatus().isOk

                // THEN: Проверяем расчет стоимости
                webTestClient.get()
                    .uri("/api/v1/portfolios/$userId/total-value")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(PortfolioValueResponse::class.java)
                    .consumeWith { result ->
                        val response = result.responseBody!!
                        val expectedTotal = btcPrice.multiply(BigDecimal.valueOf(quantity))

                        response.userId shouldBe userId
                        response.totalValue shouldBe expectedTotal.toDouble()
                    }
            }

            should("return 404 when calculating value for non-existent portfolio") {
                val nonExistentUserId = UUID.randomUUID()

                webTestClient.get()
                    .uri("/api/v1/portfolios/$nonExistentUserId/total-value")
                    .exchange()
                    .expectStatus().isNotFound
            }
        }
    }
}
