package com.cryptoalert.notification.application

import com.cryptoalert.notification.domain.NotificationSender
import com.cryptoalert.shared.event.NotificationRequestedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

/**
 * Orchestrates notification delivery.
 *
 * Selects the appropriate [NotificationSender] based on the requested channel.
 * Adding a new channel requires only a new [NotificationSender] implementation —
 * this class does not need to change (Open/Closed Principle).
 */
@Service
class NotificationService(
    private val senders: List<NotificationSender>,
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
        log.info { "Dispatching notification: userId=${event.userId}, channel=${event.channel}" }
        sender.send(event)
    }
}
