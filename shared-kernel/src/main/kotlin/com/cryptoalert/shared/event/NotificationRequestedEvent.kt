package com.cryptoalert.shared.event

import java.util.UUID

/**
 * Immutable event published whenever an alert is triggered and a notification
 * must be dispatched to the end user.
 *
 * The [notification-dispatcher] module is the only consumer of this event.
 * It deliberately carries no knowledge of alerts or portfolios — it is a pure
 * delivery instruction: "send [message] to [target] via [channel] for [userId]".
 *
 * @param userId   the owner of the triggered alert
 * @param message  human-readable notification body
 * @param channel  delivery channel (EMAIL or TELEGRAM)
 * @param target   channel-specific address: e-mail address or Telegram chat ID
 */
data class NotificationRequestedEvent(
    val userId: UUID,
    val message: String,
    val channel: NotificationChannel,
    val target: String,
)
