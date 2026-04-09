package com.cryptoalert.shared.event

interface PriceEventPublisher {
    suspend fun publish(event: PriceChangedEvent)
}
