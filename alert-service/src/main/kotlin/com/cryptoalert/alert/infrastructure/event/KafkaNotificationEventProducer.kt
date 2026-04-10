package com.cryptoalert.alert.infrastructure.event

import com.cryptoalert.alert.application.notification.NotificationEventPublisher
import com.cryptoalert.shared.event.NotificationRequestedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

/**
 * Publishes [NotificationRequestedEvent] to the [TOPIC] Kafka topic.
 *
 * Active when [app.events.type] = kafka.
 * The [KafkaTemplate] bean is provided by [com.cryptoalert.alert.config.KafkaNotificationProducerConfig].
 */
@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaNotificationEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, NotificationRequestedEvent>,
) : NotificationEventPublisher {

    private val log = KotlinLogging.logger {}

    override suspend fun publish(event: NotificationRequestedEvent) {
        kafkaTemplate.send(TOPIC, event.userId.toString(), event).await()
        log.debug { "Notification event published to Kafka: userId=${event.userId}, channel=${event.channel}" }
    }

    companion object {
        const val TOPIC = "notification-events"
    }
}
