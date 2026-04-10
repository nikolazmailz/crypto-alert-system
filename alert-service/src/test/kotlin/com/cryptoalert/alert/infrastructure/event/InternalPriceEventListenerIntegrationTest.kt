package com.cryptoalert.alert.infrastructure.event

import com.cryptoalert.alert.AlertsBaseIntegrationTest
import com.cryptoalert.alert.domain.AlertCondition
import com.cryptoalert.shared.event.PriceChangedEvent
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
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
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "BTCUSDT", BigDecimal("60000.00"), AlertCondition.GREATER_THAN)

                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "BTCUSDT", price = BigDecimal("65000.00")),
                )

                eventually(5.seconds) {
                    fetchIsActive(alertId) shouldBe false
                }
            }

            should("не трогать GREATER_THAN алерт, если цена ниже порога") {
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "BTCUSDT", BigDecimal("70000.00"), AlertCondition.GREATER_THAN)

                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "BTCUSDT", price = BigDecimal("65000.00")),
                )

                eventually(5.seconds) {
                    fetchIsActive(alertId) shouldBe true
                }
            }

            should("пометить LESS_THAN алерт как отработавший, когда цена упала ниже порога") {
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "ETHUSDT", BigDecimal("3000.00"), AlertCondition.LESS_THAN)

                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "ETHUSDT", price = BigDecimal("2800.00")),
                )

                eventually(5.seconds) {
                    fetchIsActive(alertId) shouldBe false
                }
            }

            should("не обрабатывать алерты другого символа") {
                val alertId = UUID.randomUUID()
                insertAlert(alertId, UUID.randomUUID(), "BTCUSDT", BigDecimal("60000.00"), AlertCondition.GREATER_THAN)

                applicationEventPublisher.publishEvent(
                    PriceChangedEvent(symbol = "ETHUSDT", price = BigDecimal("65000.00")),
                )

                eventually(5.seconds) {
                    fetchIsActive(alertId) shouldBe true
                }
            }

            should("обработать несколько алертов одного символа за одно событие") {
                val alertId1 = UUID.randomUUID()
                val alertId2 = UUID.randomUUID()
                insertAlert(alertId1, UUID.randomUUID(), "BTCUSDT", BigDecimal("50000.00"), AlertCondition.GREATER_THAN)
                insertAlert(alertId2, UUID.randomUUID(), "BTCUSDT", BigDecimal("55000.00"), AlertCondition.GREATER_THAN)

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
}
