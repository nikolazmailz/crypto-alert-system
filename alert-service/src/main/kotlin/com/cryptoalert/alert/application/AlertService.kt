package com.cryptoalert.alert.application

import com.cryptoalert.alert.application.notification.NotificationEventPublisher
import com.cryptoalert.alert.application.notification.toNotificationRequestedEvent
import com.cryptoalert.alert.domain.Alert
import com.cryptoalert.alert.domain.AlertCondition
import com.cryptoalert.alert.domain.AlertRepository
import com.cryptoalert.shared.error.ResourceNotFoundException
import com.cryptoalert.shared.observability.observeSuspend
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

/**
 * Слой Application (use-case):
 *  - Создание и деактивация алертов (CRUD).
 *  - [processPriceChange]: обрабатывает событие изменения цены, определяет
 *    сработавшие алерты и публикует уведомления.
 *
 * Инструментация выполнена через [ObservationRegistry] (Spring 6 / Micrometer 1.12).
 * Каждый сработавший алерт фиксируется одной Observation:
 *  - Метрика в Prometheus: `crypto_alert_triggered_seconds_{count,sum,max}`
 *    с тегами `symbol` и `condition`.
 *  - При наличии OTel-экспортёра — автоматически создаётся span трассировки.
 *
 * Инструментация живёт в Application-слое, а не в Domain — чтобы не загрязнять
 * доменные объекты кодом инфраструктуры (принцип Clean Architecture).
 */
@Service
class AlertService(
    private val alertRepository: AlertRepository,
    private val notificationEventPublisher: NotificationEventPublisher,
    private val observationRegistry: ObservationRegistry,
) {
    private val log = KotlinLogging.logger {}

    suspend fun createAlert(
        userId: UUID,
        symbol: String,
        targetPrice: BigDecimal,
        condition: AlertCondition,
    ): Alert {
        log.info { "Creating alert: userId=$userId, symbol=$symbol, targetPrice=$targetPrice, condition=$condition" }

        val alert = Alert(
            userId = userId,
            symbol = symbol.uppercase(),
            targetPrice = targetPrice,
            condition = condition,
        )
        val savedId = alertRepository.save(alert)

        log.info { "Alert created with id=$savedId" }
        return alert.copy(id = savedId)
    }

    suspend fun deactivateAlert(id: UUID) {
        log.info { "Deactivating alert id=$id" }

        alertRepository.findById(id)
            ?: throw ResourceNotFoundException("Alert not found: $id")

        alertRepository.deactivate(id)

        log.info { "Alert id=$id successfully deactivated" }
    }

    /**
     * Обрабатывает событие изменения цены и уведомляет пользователей.
     *
     * Каждый сработавший алерт обёрнут в [Observation] с тегами:
     *  - `symbol`    — торговая пара (low cardinality: BTCUSDT, ETHUSDT…)
     *  - `condition` — тип условия (GREATER_THAN | LESS_THAN)
     *
     * В Prometheus это даёт метрику:
     *   `crypto_alert_triggered_seconds_count{symbol="BTCUSDT",condition="GREATER_THAN"}`
     * — счётчик сработавших алертов, и `_sum`/`_max` — для latency-анализа.
     */
    suspend fun processPriceChange(symbol: String, price: BigDecimal) {
        log.info { "processPriceChange: symbol=$symbol at price=$price" }

        val triggeredAlerts = alertRepository.findTriggeredAlerts(symbol, price)

        triggeredAlerts.forEach { alert ->
            Observation.createNotStarted(METRIC_ALERT_TRIGGERED, observationRegistry)
                .lowCardinalityKeyValue("symbol", alert.symbol)
                .lowCardinalityKeyValue("condition", alert.condition.name)
                .observeSuspend {
                    log.info { "ALERT TRIGGERED: userId=${alert.userId} for $symbol at price=$price" }
                    notificationEventPublisher.publish(alert.toNotificationRequestedEvent(price))
                    alertRepository.markAsSent(alert.id)
                }
        }
    }

    companion object {
        /**
         * Имя Observation → Prometheus-метрика:
         *   `crypto_alert_triggered_seconds_count` (counter)
         *   `crypto_alert_triggered_seconds_sum`   (total latency)
         *   `crypto_alert_triggered_seconds_max`   (max latency, 2-min window)
         */
        private const val METRIC_ALERT_TRIGGERED = "crypto.alert.triggered"
    }
}
