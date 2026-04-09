package com.cryptoalert.alert.application

import com.cryptoalert.alert.domain.AlertRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AlertService(
    private val alertRepository: AlertRepository,
//    private val notificationService: NotificationService
) {
    private val log = KotlinLogging.logger {}

    suspend fun processPriceChange(symbol: String, price: BigDecimal) {
        log.info { "processPriceChange: symbol is $symbol at price $price" }

        val triggeredAlerts = alertRepository.findTriggeredAlerts(symbol, price)

        triggeredAlerts.forEach { alert ->
            log.info { "ALERT TRIGGERED: User ${alert.userId}for $symbol at price $price" }
//            notificationService.send(alert, price)
            alertRepository.markAsSent(alert.id)
        }

    }
}
