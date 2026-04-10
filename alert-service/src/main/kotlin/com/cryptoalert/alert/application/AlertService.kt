package com.cryptoalert.alert.application

import com.cryptoalert.alert.application.notification.toNotificationRequestedEvent
import com.cryptoalert.alert.application.notification.NotificationEventPublisher
import com.cryptoalert.alert.domain.Alert
import com.cryptoalert.alert.domain.AlertCondition
import com.cryptoalert.alert.domain.AlertRepository
import com.cryptoalert.shared.error.ResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class AlertService(
    private val alertRepository: AlertRepository,
    private val notificationEventPublisher: NotificationEventPublisher,
) {
    private val log = KotlinLogging.logger {}

    suspend fun createAlert(
        userId: UUID,
        symbol: String,
        targetPrice: BigDecimal,
        condition: AlertCondition,
    ): Alert {
        log.info { "Creating alert: userId=$userId, symbol=$symbol, targetPrice=$targetPrice, condition=$condition" }

        val alert = Alert(
            userId = userId,
            symbol = symbol.uppercase(),
            targetPrice = targetPrice,
            condition = condition,
        )
        val savedId = alertRepository.save(alert)

        log.info { "Alert created with id=$savedId" }
        return alert.copy(id = savedId)
    }

    suspend fun deactivateAlert(id: UUID) {
        log.info { "Deactivating alert id=$id" }

        alertRepository.findById(id)
            ?: throw ResourceNotFoundException("Alert not found: $id")

        alertRepository.deactivate(id)

        log.info { "Alert id=$id successfully deactivated" }
    }

    suspend fun processPriceChange(symbol: String, price: BigDecimal) {
        log.info { "processPriceChange: symbol=$symbol at price=$price" }

        val triggeredAlerts = alertRepository.findTriggeredAlerts(symbol, price)

        triggeredAlerts.forEach { alert ->
            log.info { "ALERT TRIGGERED: userId=${alert.userId} for $symbol at price=$price" }
            notificationEventPublisher.publish(alert.toNotificationRequestedEvent(price))
            alertRepository.markAsSent(alert.id)
        }
    }
}
