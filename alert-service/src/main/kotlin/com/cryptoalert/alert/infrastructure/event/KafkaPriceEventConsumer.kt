package com.cryptoalert.alert.infrastructure.event

import com.cryptoalert.alert.application.AlertService
import com.cryptoalert.shared.event.PriceChangedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaPriceEventConsumer(
    private val alertService: AlertService,
) {

    private val log = KotlinLogging.logger {}
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @KafkaListener(
        topics = [TOPIC],
        groupId = "alert-service",
        containerFactory = "priceEventListenerContainerFactory",
    )
    fun handle(event: PriceChangedEvent) {
        log.info { "Received Kafka price event: symbol=${event.symbol}, price=${event.price}" }
        applicationScope.launch {
            alertService.processPriceChange(event.symbol, event.price)
        }
    }

    companion object {
        const val TOPIC = "price-events"
    }
}
