package com.cryptoalert.notification.config

import com.cryptoalert.shared.event.NotificationRequestedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

/**
 * Kafka consumer factory for [NotificationRequestedEvent] messages.
 *
 * Active when [app.events.type] = kafka.
 * [JsonDeserializer.USE_TYPE_INFO_HEADERS] is disabled so the consumer is
 * not coupled to the producer's type headers — it always deserializes into
 * [NotificationRequestedEvent] via [JsonDeserializer.VALUE_DEFAULT_TYPE].
 */
@EnableKafka
@Configuration
@ConditionalOnProperty(name = ["app.events.type"], havingValue = "kafka")
class KafkaNotificationConsumerConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
) {
    @Bean
    fun notificationEventListenerContainerFactory():
        ConcurrentKafkaListenerContainerFactory<String, NotificationRequestedEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            JsonDeserializer.TRUSTED_PACKAGES to "com.cryptoalert.shared.event",
            JsonDeserializer.VALUE_DEFAULT_TYPE to NotificationRequestedEvent::class.java.name,
            JsonDeserializer.USE_TYPE_INFO_HEADERS to "false",
        )
        val consumerFactory = DefaultKafkaConsumerFactory<String, NotificationRequestedEvent>(props)
        return ConcurrentKafkaListenerContainerFactory<String, NotificationRequestedEvent>().apply {
            this.consumerFactory = consumerFactory
        }
    }
}
