package com.cryptoalert.notification.infrastructure.email

import com.cryptoalert.notification.domain.NotificationSender
import com.cryptoalert.shared.event.NotificationChannel
import com.cryptoalert.shared.event.NotificationRequestedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

/**
 * Delivers notifications via SMTP using Spring's [JavaMailSender].
 *
 * Active only when [notification.channels.email.enabled] = true.
 * The blocking [JavaMailSender.send] call is dispatched to [Dispatchers.IO]
 * so it never blocks the Netty event loop.
 */
@Component
@ConditionalOnProperty(name = ["notification.channels.email.enabled"], havingValue = "true")
class EmailNotificationSender(
    private val mailSender: JavaMailSender,
) : NotificationSender {

    private val log = KotlinLogging.logger {}

    override val channel: NotificationChannel = NotificationChannel.EMAIL

    override suspend fun send(event: NotificationRequestedEvent): Unit = withContext(Dispatchers.IO) {
        log.info { "Sending email to ${event.target} for userId=${event.userId}" }

        val message = SimpleMailMessage().apply {
            setTo(event.target)
            subject = "Crypto Alert Notification"
            text = event.message
        }
        mailSender.send(message)

        log.info { "Email successfully sent to ${event.target}" }
    }
}
