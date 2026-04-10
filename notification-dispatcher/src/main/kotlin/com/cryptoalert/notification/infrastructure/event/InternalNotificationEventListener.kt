package com.cryptoalert.notification.infrastructure.event

import com.cryptoalert.notification.application.NotificationService
import com.cryptoalert.shared.event.NotificationRequestedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Receives [NotificationRequestedEvent] from Spring's in-process event bus.
 *
 * Active when [app.events.type] = internal (default).
 * The coroutine scope is backed by [SupervisorJob] so a failure in one
 * notification does not cancel sibling coroutines.
 */
@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "internal", matchIfMissing = true)
class InternalNotificationEventListener(
    private val notificationService: NotificationService,
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handle(event: NotificationRequestedEvent) {
        log.info { "Received internal notification event: userId=${event.userId}, channel=${event.channel}" }
        applicationScope.launch {
            notificationService.send(event)
        }
    }
}
