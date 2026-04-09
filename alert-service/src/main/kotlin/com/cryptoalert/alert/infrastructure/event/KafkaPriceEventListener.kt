package com.cryptoalert.alert.infrastructure.event

import com.cryptoalert.alert.application.AlertService
import com.cryptoalert.shared.event.PriceChangedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaPriceEventListener(
    private val alertService: AlertService,
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    @KafkaListener(topics = ["price-events"], groupId = "alert-service-group")
    fun handle(event: PriceChangedEvent) = applicationScope.launch {
        alertService.processPriceChange(event.symbol, event.price)
    }
}
