package com.cryptoalert.alert.infrastructure.event

import com.cryptoalert.alert.application.notification.NotificationEventPublisher
import com.cryptoalert.shared.event.NotificationRequestedEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * Publishes [NotificationRequestedEvent] via Spring's in-process event bus.
 *
 * Active when [app.events.type] = internal (default).
 * The event is picked up by [com.cryptoalert.notification.infrastructure.event.InternalNotificationEventListener]
 * in the notification-dispatcher module (same JVM / Spring context).
 */
@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "internal", matchIfMissing = true)
class SpringNotificationEventPublisher(
    private val eventPublisher: ApplicationEventPublisher,
) : NotificationEventPublisher {

    override suspend fun publish(event: NotificationRequestedEvent) {
        eventPublisher.publishEvent(event)
    }
}
