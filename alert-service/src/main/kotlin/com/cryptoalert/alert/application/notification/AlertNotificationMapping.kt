package com.cryptoalert.alert.application.notification

import com.cryptoalert.alert.domain.Alert
import com.cryptoalert.alert.domain.AlertCondition
import com.cryptoalert.shared.event.NotificationChannel
import com.cryptoalert.shared.event.NotificationRequestedEvent
import java.math.BigDecimal

/**
 * Maps a triggered [Alert] to a [NotificationRequestedEvent].
 *
 * [channel] and [target] are resolved with sensible defaults for this skeleton
 * implementation. In production these would be sourced from a User Preferences
 * service (e.g. the user's stored e-mail or Telegram chat ID).
 *
 * @param triggeredPrice the live market price that caused the alert to fire
 */
fun Alert.toNotificationRequestedEvent(triggeredPrice: BigDecimal): NotificationRequestedEvent =
    NotificationRequestedEvent(
        userId = userId,
        message = buildAlertMessage(symbol, triggeredPrice, condition, targetPrice),
        // TODO: resolve from user-preferences service
        channel = NotificationChannel.EMAIL,
        // TODO: resolve from user-preferences service
        target = "$userId@crypto-alert.example.com",
    )

private fun buildAlertMessage(
    symbol: String,
    triggeredPrice: BigDecimal,
    condition: AlertCondition,
    targetPrice: BigDecimal,
): String {
    val verb = when (condition) {
        AlertCondition.GREATER_THAN -> "exceeded"
        AlertCondition.LESS_THAN -> "dropped below"
    }
    return "$symbol has $verb your target of $targetPrice. Current price: $triggeredPrice"
}
