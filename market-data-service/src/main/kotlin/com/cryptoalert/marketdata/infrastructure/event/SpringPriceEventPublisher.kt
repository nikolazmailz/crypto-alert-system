package com.cryptoalert.marketdata.infrastructure.event

import com.cryptoalert.shared.event.PriceChangedEvent
import com.cryptoalert.shared.event.PriceEventPublisher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "internal", matchIfMissing = true)
class SpringPriceEventPublisher(
    private val eventPublisher: ApplicationEventPublisher
) : PriceEventPublisher {
    override suspend fun publish(event: PriceChangedEvent) {
        eventPublisher.publishEvent(event)
    }
}
