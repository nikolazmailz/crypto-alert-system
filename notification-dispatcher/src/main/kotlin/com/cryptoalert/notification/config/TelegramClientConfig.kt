package com.cryptoalert.notification.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * Creates the [WebClient] bean pre-configured for the Telegram Bot API.
 *
 * The base URL includes the bot token so individual senders do not need to
 * know the token — they only specify the API method path (e.g. /sendMessage).
 *
 * Created only when the Telegram channel is enabled.
 */
@Configuration
@ConditionalOnProperty(name = ["notification.channels.telegram.enabled"], havingValue = "true")
class TelegramClientConfig(
    @Value("\${telegram.bot.token}") private val botToken: String,
) {
    @Bean("telegramWebClient")
    fun telegramWebClient(): WebClient = WebClient.builder()
        .baseUrl("https://api.telegram.org/bot$botToken")
        .build()
}
