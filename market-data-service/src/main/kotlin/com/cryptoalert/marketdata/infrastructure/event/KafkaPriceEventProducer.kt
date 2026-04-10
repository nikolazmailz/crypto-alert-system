package com.cryptoalert.marketdata.infrastructure.event

import com.cryptoalert.shared.event.PriceChangedEvent
import com.cryptoalert.shared.event.PriceEventPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaPriceEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, PriceChangedEvent>,
) : PriceEventPublisher {

    private val log = KotlinLogging.logger {}

    override suspend fun publish(event: PriceChangedEvent) {
        kafkaTemplate.send(TOPIC, event.symbol, event).await()
        log.debug { "Price event published to Kafka: symbol=${event.symbol}, price=${event.price}" }
    }

    companion object {
        const val TOPIC = "price-events"
    }
}
