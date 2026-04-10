package com.cryptoalert.notification.infrastructure.telegram

import com.cryptoalert.notification.domain.NotificationSender
import com.cryptoalert.shared.event.NotificationChannel
import com.cryptoalert.shared.event.NotificationRequestedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity

/**
 * Delivers notifications via the Telegram Bot API.
 *
 * Uses [WebClient] (already reactive/non-blocking) to POST a sendMessage request.
 * [event.target] is expected to be the numeric Telegram chat ID as a string.
 *
 * Active only when [notification.channels.telegram.enabled] = true.
 * The [WebClient] bean is provided by [TelegramClientConfig].
 */
@Component
@ConditionalOnProperty(name = ["notification.channels.telegram.enabled"], havingValue = "true")
class TelegramNotificationSender(
    private val telegramWebClient: WebClient,
) : NotificationSender {

    private val log = KotlinLogging.logger {}

    override val channel: NotificationChannel = NotificationChannel.TELEGRAM

    override suspend fun send(event: NotificationRequestedEvent) {
        log.info { "Sending Telegram message to chatId=${event.target} for userId=${event.userId}" }

        telegramWebClient.get()
            .uri { builder ->
                builder.path("/sendMessage")
                    .queryParam("chat_id", event.target)
                    .queryParam("text", event.message)
                    .build()
            }
            .retrieve()
            .awaitBodilessEntity()

        log.info { "Telegram message successfully sent to chatId=${event.target}" }
    }
}
