package com.cryptoalert.alert.infrastructure.event

import com.cryptoalert.alert.application.AlertService
import com.cryptoalert.shared.event.PriceChangedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "internal", matchIfMissing = true)
class InternalPriceEventListener(
    private val alertService: AlertService,
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
//    @EventListener
//    suspend fun handle(event: PriceChangedEvent) {
//        alertService.processPriceChange(event.symbol, event.price)
//    }

    @EventListener
    fun handle(event: PriceChangedEvent) {
        applicationScope.launch {
            alertService.processPriceChange(event.symbol, event.price)
        }
    }
}
