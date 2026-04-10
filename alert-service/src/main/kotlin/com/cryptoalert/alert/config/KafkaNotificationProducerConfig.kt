package com.cryptoalert.alert.config

import com.cryptoalert.shared.event.NotificationRequestedEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer

/**
 * Provides a [KafkaTemplate] for publishing [NotificationRequestedEvent] messages.
 *
 * Active when [app.events.type] = kafka.
 * A dedicated producer factory is used to avoid type-parameter conflicts with
 * the existing [PriceChangedEvent] template in other modules.
 */
@Configuration
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaNotificationProducerConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
) {
    @Bean
    fun notificationEventKafkaTemplate(): KafkaTemplate<String, NotificationRequestedEvent> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to "1",
            ProducerConfig.LINGER_MS_CONFIG to "5",
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "lz4",
        )
        return KafkaTemplate(DefaultKafkaProducerFactory(props))
    }
}
