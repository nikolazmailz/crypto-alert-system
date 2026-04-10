package com.cryptoalert.notification.infrastructure.event

import com.cryptoalert.notification.application.NotificationService
import com.cryptoalert.shared.event.NotificationRequestedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Consumes [NotificationRequestedEvent] from the [TOPIC] Kafka topic.
 *
 * Active when [app.events.type] = kafka.
 * Deserialization is configured in [KafkaNotificationConsumerConfig].
 */
@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaNotificationEventConsumer(
    private val notificationService: NotificationService,
) {
    private val log = KotlinLogging.logger {}
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @KafkaListener(
        topics = [TOPIC],
        groupId = "notification-dispatcher",
        containerFactory = "notificationEventListenerContainerFactory",
    )
    fun handle(event: NotificationRequestedEvent) {
        log.info { "Received Kafka notification event: userId=${event.userId}, channel=${event.channel}" }
        applicationScope.launch {
            notificationService.send(event)
        }
    }

    companion object {
        const val TOPIC = "notification-events"
    }
}
