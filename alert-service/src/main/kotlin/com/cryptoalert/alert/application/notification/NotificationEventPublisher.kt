package com.cryptoalert.alert.application.notification

import com.cryptoalert.shared.event.NotificationRequestedEvent

/**
 * Application-layer port for publishing [com.cryptoalert.shared.event.NotificationRequestedEvent].
 *
 * Two implementations exist (chosen via `app.events.type`):
 * - [com.cryptoalert.alert.infrastructure.event.SpringNotificationEventPublisher] — internal Spring events
 * - [com.cryptoalert.alert.infrastructure.event.KafkaNotificationEventProducer]  — Kafka topic
 */
interface NotificationEventPublisher {
    suspend fun publish(event: NotificationRequestedEvent)
}
