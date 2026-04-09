package com.cryptoalert.marketdata.infrastructure.event

import com.cryptoalert.shared.event.PriceChangedEvent
import com.cryptoalert.shared.event.PriceEventPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaPriceEventPublisher(
    private val streamBridge: StreamBridge,
) : PriceEventPublisher {
    private val log = KotlinLogging.logger {}

    override suspend fun publish(event: PriceChangedEvent) {
        val binding = "price-events-out-0"
        withContext(Dispatchers.IO) {
            val message = MessageBuilder
                .withPayload(event)
                .setHeader("kafka_messageKey", event.javaClass.name)
                .setHeader("x-source", "market-data-service")
                .build()
            streamBridge.send(binding, message)
                .also { sent ->
                    if (sent) log.info { "Sent to binding=$binding key=${event.javaClass.name}" }
                    else log.error { "Failed to send to binding=$binding} key=${event.javaClass.name}" }
                }
        }
    }
}
