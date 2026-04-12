package com.cryptoalert.alert.controller

import com.cryptoalert.alert.AlertsBaseIntegrationTest
import com.cryptoalert.alert.domain.AlertCondition
import com.cryptoalert.alert.dto.AlertResponse
import com.cryptoalert.alert.dto.CreateAlertRequest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.math.BigDecimal
import java.util.UUID

/**
 * Интеграционный тест для [AlertController].
 *
 * Проверяет полный HTTP-цикл управления алертами:
 *  - создание алерта через POST → 201 Created
 *  - деактивацию через DELETE → 204 No Content
 *  - ответ 404 при обращении к несуществующему алерту
 *  - сквозной lifecycle: создание → проверка БД → деактивация → проверка БД
 */
class AlertControllerIntegrationTest(
    private val webTestClient: WebTestClient,
) : AlertsBaseIntegrationTest() {

    init {
        beforeTest { runBlocking { deleteAllAlerts() } }

        context("POST /api/v1/alerts") {

            should("вернуть 201 Created и тело алерта при корректном запросе") {
                val userId = UUID.randomUUID()
                val request = CreateAlertRequest(
                    userId = userId,
                    symbol = "btcusdt",        // намеренно lowercase — сервис должен uppercase
                    targetPrice = 65_000.0,
                    condition = CreateAlertRequest.Condition.GREATER_THAN,
                )

                val response = webTestClient.post()
                    .uri("/api/v1/alerts")
                    .header("Authorization", bearerToken(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<AlertResponse>()
                    .returnResult()
                    .responseBody

                response shouldNotBe null
                response!!.userId shouldBe userId
                response.symbol shouldBe "BTCUSDT"
                response.targetPrice shouldBe 65_000.0
                response.condition shouldBe AlertResponse.Condition.GREATER_THAN
                response.isActive shouldBe true
            }

            should("сохранить алерт в БД после создания") {
                val userId = UUID.randomUUID()
                val request = CreateAlertRequest(
                    userId = userId,
                    symbol = "ETHUSDT",
                    targetPrice = 3_000.0,
                    condition = CreateAlertRequest.Condition.LESS_THAN,
                )

                val response = webTestClient.post()
                    .uri("/api/v1/alerts")
                    .header("Authorization", bearerToken(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<AlertResponse>()
                    .returnResult()
                    .responseBody!!

                // Проверяем, что запись появилась в БД с is_active = true
                fetchIsActive(response.id) shouldBe true
            }
        }

        context("DELETE /api/v1/alerts/{id}") {

            should("вернуть 204 No Content и деактивировать алерт") {
                val alertId = UUID.randomUUID()
                insertAlert(
                    id = alertId,
                    userId = UUID.randomUUID(),
                    symbol = "SOLUSDT",
                    targetPrice = BigDecimal("150.00"),
                    condition = AlertCondition.GREATER_THAN,
                )

                webTestClient.delete()
                    .uri("/api/v1/alerts/$alertId")
                    .header("Authorization", bearerToken(UUID.randomUUID()))
                    .exchange()
                    .expectStatus().isNoContent

                // Алерт должен остаться в БД, но is_active = false
                fetchIsActive(alertId) shouldBe false
            }

            should("вернуть 404 Not Found для несуществующего алерта") {
                val nonExistentId = UUID.randomUUID()

                webTestClient.delete()
                    .uri("/api/v1/alerts/$nonExistentId")
                    .header("Authorization", bearerToken(UUID.randomUUID()))
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        context("Полный жизненный цикл алерта") {

            should("создать алерт → проверить в БД → деактивировать → проверить статус в БД") {
                val userId = UUID.randomUUID()

                // Шаг 1: Создать алерт через API
                val created = webTestClient.post()
                    .uri("/api/v1/alerts")
                    .header("Authorization", bearerToken(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                        CreateAlertRequest(
                            userId = userId,
                            symbol = "BNBUSDT",
                            targetPrice = 500.0,
                            condition = CreateAlertRequest.Condition.LESS_THAN,
                        ),
                    )
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<AlertResponse>()
                    .returnResult()
                    .responseBody!!

                // Шаг 2: Убедиться, что алерт активен в БД
                fetchIsActive(created.id) shouldBe true

                // Шаг 3: Деактивировать алерт через API
                webTestClient.delete()
                    .uri("/api/v1/alerts/${created.id}")
                    .header("Authorization", bearerToken(userId))
                    .exchange()
                    .expectStatus().isNoContent

                // Шаг 4: Убедиться, что алерт деактивирован в БД
                fetchIsActive(created.id) shouldBe false
            }

            should("создать два алерта для разных пользователей — деактивация одного не влияет на другой") {
                val userId1 = UUID.randomUUID()
                val userId2 = UUID.randomUUID()

                print("userId1 $userId1")
                print("userId2 $userId2")

                val alert1 = webTestClient.post()
                    .uri("/api/v1/alerts")
                    .header("Authorization", bearerToken(userId1))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                        CreateAlertRequest(
                            userId = userId1,
                            symbol = "ADAUSDT",
                            targetPrice = 1.0,
                            condition = CreateAlertRequest.Condition.GREATER_THAN,
                        ),
                    )
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<AlertResponse>()
                    .returnResult()
                    .responseBody!!

                val alert2 = webTestClient.post()
                    .uri("/api/v1/alerts")
                    .header("Authorization", bearerToken(userId2))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                        CreateAlertRequest(
                            userId = userId2,
                            symbol = "ADAUSDT",
                            targetPrice = 1.0,
                            condition = CreateAlertRequest.Condition.GREATER_THAN,
                        ),
                    )
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody<AlertResponse>()
                    .returnResult()
                    .responseBody!!

                // Деактивируем только первый
                webTestClient.delete()
                    .uri("/api/v1/alerts/${alert1.id}")
                    .header("Authorization", bearerToken(userId1))
                    .exchange()
                    .expectStatus().isNoContent

                // Первый деактивирован, второй остался активным
                fetchIsActive(alert1.id) shouldBe false
                fetchIsActive(alert2.id) shouldBe true
            }
        }
    }
}
