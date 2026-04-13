package com.cryptoalert.notification.application

import com.cryptoalert.notification.domain.NotificationSender
import com.cryptoalert.shared.event.NotificationRequestedEvent
import com.cryptoalert.shared.observability.observeSuspend
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.springframework.stereotype.Service

/**
 * Orchestrates notification delivery.
 *
 * Selects the appropriate [NotificationSender] based on the requested channel.
 * Adding a new channel requires only a new [NotificationSender] implementation —
 * this class does not need to change (Open/Closed Principle).
 *
 * Инструментация:
 *  Каждая успешная отправка фиксируется через [ObservationRegistry].
 *  Prometheus-метрика: `crypto_notification_sent_seconds_count{channel="EMAIL|TELEGRAM"}`
 *  При ошибке Observation записывает её и пробрасывает исключение вызывающей стороне.
 */
@Service
class NotificationService(
    private val senders: List<NotificationSender>,
    private val observationRegistry: ObservationRegistry,
) {
    private val log = KotlinLogging.logger {}

    suspend fun send(event: NotificationRequestedEvent) {
        val sender = senders.find { it.channel == event.channel }
            ?: run {
                log.warn {
                    "No sender registered for channel=${event.channel}. " +
                        "Dropping notification for userId=${event.userId}."
                }
                return
            }

        Observation.createNotStarted(METRIC_NOTIFICATION_SENT, observationRegistry)
            // channel — low cardinality: только EMAIL и TELEGRAM.
            .lowCardinalityKeyValue("channel", event.channel.name)
            .observeSuspend {
                log.info { "Dispatching notification: userId=${event.userId}, channel=${event.channel}" }
                sender.send(event)
            }
    }

    companion object {
        /**
         * Имя Observation → Prometheus-метрика:
         *   `crypto_notification_sent_seconds_count{channel="EMAIL"}` — счётчик отправок
         *   `crypto_notification_sent_seconds_sum`                    — суммарное время
         *
         * Добавление нового канала в [NotificationSender] автоматически
         * появится в метриках без изменений здесь.
         */
        private const val METRIC_NOTIFICATION_SENT = "crypto.notification.sent"
    }
}
