package com.cryptoalert.alert.infrastructure.event

import com.cryptoalert.alert.AlertsBaseIntegrationTest
import com.cryptoalert.alert.domain.AlertCondition
import com.cryptoalert.shared.event.PriceChangedEvent
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * Интеграционный тест для [InternalPriceEventListener].
 *
 * Проверяет, что слушатель корректно обрабатывает Spring-события [PriceChangedEvent]:
 * находит подходящие алерты в БД, помечает их как отработавшие (is_active = false)
 * и не трогает алерты, условие по которым не выполнено.
 *
 * Профиль "internal" активен по умолчанию (matchIfMissing = true),
 * поэтому дополнительных настроек не требуется.
 */
class InternalPriceEventListenerIntegrationTest(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : AlertsBaseIntegrationTest() {

    init {
        beforeTest { runBlocking { deleteAllAlerts() } }

        context("InternalPriceEventListener") {

            should("пометить GREATER_THAN алерт как отработавший, когда цена превысила порог") {
                // Arrange
                val alertId = UUID.randomUUID()
                insertAlert(
                    id = alertId,
                    userId = UUID.randomUUID(),
                    symbol = "BTCUSDT",
                    targetPrice = BigDecimal("60000.00"),
                    condition = AlertCondition.GREATER_THAN,
                )

                // Act — публикуем событие с ценой ВЫШЕ порога
                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "BTCUSDT", price = BigDecimal("65000.00")),
                )

                // Assert — ждём завершения асинхронного корутин-обработчика
                eventually(5.seconds) {
                    fetchIsActive(alertId) shouldBe false
                }
            }

            should("не трогать GREATER_THAN алерт, если цена ниже порога") {
                // Arrange
                val alertId = UUID.randomUUID()
                insertAlert(
                    id = alertId,
                    userId = UUID.randomUUID(),
                    symbol = "BTCUSDT",
                    targetPrice = BigDecimal("70000.00"),
                    condition = AlertCondition.GREATER_THAN,
                )

                // Act — цена НИЖЕ порога, алерт не должен сработать
                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "BTCUSDT", price = BigDecimal("65000.00")),
                )

                eventually(5.seconds) {
                    fetchIsActive(alertId) shouldBe true
                }
            }

            should("пометить LESS_THAN алерт как отработавший, когда цена упала ниже порога") {
                // Arrange
                val alertId = UUID.randomUUID()
                insertAlert(
                    id = alertId,
                    userId = UUID.randomUUID(),
                    symbol = "ETHUSDT",
                    targetPrice = BigDecimal("3000.00"),
                    condition = AlertCondition.LESS_THAN,
                )

                // Act — цена НИЖЕ порога
                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "ETHUSDT", price = BigDecimal("2800.00")),
                )

                eventually(5.seconds) {
                    fetchIsActive(alertId) shouldBe false
                }
            }

            should("не обрабатывать алерты другого символа") {
                // Arrange — алерт на BTCUSDT
                val alertId = UUID.randomUUID()
                insertAlert(
                    id = alertId,
                    userId = UUID.randomUUID(),
                    symbol = "BTCUSDT",
                    targetPrice = BigDecimal("60000.00"),
                    condition = AlertCondition.GREATER_THAN,
                )

                // Act — событие приходит для ETHUSDT, не для BTCUSDT
                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "ETHUSDT", price = BigDecimal("65000.00")),
                )

                eventually(5.seconds) {
                    // BTCUSDT-алерт должен остаться активным
                    fetchIsActive(alertId) shouldBe true
                }
            }

            should("обработать несколько алертов одного символа за одно событие") {
                // Arrange — два алерта на один символ, оба должны сработать
                val alertId1 = UUID.randomUUID()
                val alertId2 = UUID.randomUUID()
                insertAlert(
                    alertId1,
                    UUID.randomUUID(),
                    "BTCUSDT",
                    BigDecimal("50000.00"),
                    AlertCondition.GREATER_THAN,
                )
                insertAlert(
                    alertId2,
                    UUID.randomUUID(),
                    "BTCUSDT",
                    BigDecimal("55000.00"),
                    AlertCondition.GREATER_THAN,
                )

                // Act
                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "BTCUSDT", price = BigDecimal("65000.00")),
                )

                eventually(5.seconds) {
                    fetchIsActive(alertId1) shouldBe false
                    fetchIsActive(alertId2) shouldBe false
                }
            }
        }
    }

    // ── Вспомогательные методы ────────────────────────────────────────────────

    private suspend fun insertAlert(
        id: UUID,
        userId: UUID,
        symbol: String,
        targetPrice: BigDecimal,
        condition: AlertCondition,
    ) {
        databaseClient.sql(
            """
            INSERT INTO alerts (id, user_id, symbol, target_price, condition, is_active, created_at)
            VALUES (:id, :userId, :symbol, :targetPrice, :condition, :isActive, :createdAt)
            """.trimIndent(),
        )
            .bind("id", id)
            .bind("userId", userId)
            .bind("symbol", symbol)
            .bind("targetPrice", targetPrice)
            .bind("condition", condition.name)
            .bind("isActive", true)
            .bind("createdAt", Instant.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private suspend fun fetchIsActive(id: UUID): Boolean? =
        databaseClient.sql("SELECT is_active FROM alerts WHERE id = :id")
            .bind("id", id)
            .fetch()
            .one()
            .map { it["is_active"] as Boolean }
            .awaitSingleOrNull()
}
