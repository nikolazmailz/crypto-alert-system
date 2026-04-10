package com.cryptoalert.marketdata.config

import com.cryptoalert.shared.event.PriceChangedEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaProducerConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
) {

    @Bean
    fun priceEventKafkaTemplate(): KafkaTemplate<String, PriceChangedEvent> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to "1",
            ProducerConfig.LINGER_MS_CONFIG to "5",
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "lz4",
            ProducerConfig.BATCH_SIZE_CONFIG to "65536",
            ProducerConfig.BUFFER_MEMORY_CONFIG to "67108864",
            ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to "120000",
            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to "10000",
            ProducerConfig.RETRY_BACKOFF_MS_CONFIG to "200",
        )
        return KafkaTemplate(DefaultKafkaProducerFactory(props))
    }
}
