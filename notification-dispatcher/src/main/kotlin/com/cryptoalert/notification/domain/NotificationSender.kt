package com.cryptoalert.notification.domain

import com.cryptoalert.shared.event.NotificationChannel
import com.cryptoalert.shared.event.NotificationRequestedEvent

/**
 * Domain port — a single delivery channel.
 *
 * Each infrastructure adapter (Email, Telegram, …) implements this interface.
 * The domain layer has zero knowledge of Spring, mail libraries, or HTTP clients.
 */
interface NotificationSender {
    /** The channel this sender is responsible for. */
    val channel: NotificationChannel

    /** Delivers the notification described by [event]. */
    suspend fun send(event: NotificationRequestedEvent)
}
